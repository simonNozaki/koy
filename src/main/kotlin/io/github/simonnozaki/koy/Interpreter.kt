package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import io.github.simonnozaki.koy.Operator.*

// TODO add a printer of tree node for debug logging
class Interpreter(
    private val functionEnvironment: MutableMap<String, FunctionDefinition> = mutableMapOf(),
    private var variableEnvironment: Environment = Environment(mutableMapOf(), null)
) {
    /**
     * Return the value from interpreter variable environment
     */
    fun getValue(name: String) = variableEnvironment.bindings[name]

    fun getFunction(name: String) = functionEnvironment[name]

    fun getFunctions() = functionEnvironment

    /**
     * Return all variables and nodes of syntax tree from environment.
     */
    fun getVariables() = variableEnvironment.bindings.toMap()

    private fun getBinaryOpsResult(binaryExpression: BinaryExpression): Value {
        val lhs = interpret(binaryExpression.lhs)
        val rhs = interpret(binaryExpression.rhs)

        val result = when (binaryExpression.operator) {
            ADD -> {
                if (lhs.isString() && rhs.isString()) {
                    lhs.asString().value + rhs.asString().value
                } else if (lhs.isInt() && rhs.isInt()) {
                    lhs.asInt().value + rhs.asInt().value
                } else {
                    throw KoyLangRuntimeException("$lhs and $rhs is not compatible on add operation")
                }
            }
            SUBTRACT -> lhs.asInt().value - rhs.asInt().value
            MULTIPLY -> lhs.asInt().value * rhs.asInt().value
            DIVIDE -> lhs.asInt().value / rhs.asInt().value
            LESS_THAN -> lhs.asInt().value < rhs.asInt().value
            LESS_OR_EQUAL -> lhs.asInt().value <= rhs.asInt().value
            GREATER_THAN -> lhs.asInt().value > rhs.asInt().value
            GREATER_OR_EQUAL -> lhs.asInt().value >= rhs.asInt().value
            EQUAL -> lhs.asInt().value == rhs.asInt().value
            NOT_EQUAL -> lhs.asInt().value != rhs.asInt().value
        }
        return Value.of(result)
    }

    fun interpret(expression: Expression): Value {
        println("|- $expression")
        if (expression is BinaryExpression) {
            return getBinaryOpsResult(expression)
        }
        if (expression is IntegerLiteral) {
            return Value.of(expression.value)
        }
        if (expression is ArrayLiteral) {
            val itemValues = expression.items.map { interpret(it) }
            return Value.of(itemValues)
        }
        if (expression is BoolLiteral) {
            return Value.of(expression.value)
        }
        if (expression is StringLiteral) {
            return Value.of(expression.value)
        }
        if (expression is ObjectLiteral) {
            // Evaluate each props value and get `Value`
            val propertiesMap = expression.properties.entries.associate { it.key to interpret(it.value) }
            return Value.ofObject(propertiesMap)
        }
        if (expression is FunctionLiteral) {
            return Value.ofFunction(expression.args, expression.body)
        }
        if (expression is Identifier) {
            // Get variable
            val bindingOptions = variableEnvironment.findBindings(expression.name)
            return bindingOptions?.let { it[expression.name] } ?: throw KoyLangRuntimeException("Identifier ${expression.name} not found")
        }
        if (expression is Assignment) {
            // Assign variable
            val bindingOptions = variableEnvironment.findBindings(expression.name)
            val value = interpret(expression.expression)
            if (bindingOptions != null) {
                bindingOptions[expression.name] = value
            } else {
                // Function literal is assigned as `FunctionDefinition`, so check value type if is `Value.Function`.
                when (value) {
                    is Value.Function -> {
                        val def = defineFunction(expression.name, value.args, value.body)
                        functionEnvironment[expression.name] = def
                    }
                    else -> variableEnvironment.bindings[expression.name] = value
                }
                variableEnvironment.bindings[expression.name] = value
            }
            return value
        }
        if (expression is PrintLn) {
            return interpret(expression.arg)
        }
        if (expression is IfExpression) {
            val condition = interpret(expression.condition).asBool().value
            return if (condition) {
                interpret(expression.thenClause)
            } else {
                expression.elseClause?.let { interpret(it) } ?: Value.of(true)
            }
        }
        if (expression is WhileExpression) {
            while (true) {
                val condition = interpret(expression.condition).asBool().value
                if (condition) {
                    interpret(expression.body)
                } else {
                    break
                }
            }
            return Value.of(true)
        }
        if (expression is BlockExpression) {
            // Block expression: interpret some expression from the top
            var v = Value.of(0)
            val iterator = expression.elements.iterator()
            while (iterator.hasNext()) {
                v = interpret(iterator.next())
            }
            return v
        }
        if (expression is FunctionCall) {
            val definition = functionEnvironment[expression.name] ?: throw KoyLangRuntimeException("Function [ ${expression.name} ] not found")

            val actualParams = expression.args
            val formalParams = definition.args
            val body = definition.body

            val values = actualParams.map { interpret(it) }

            // Called variable environment should be separated from Caller
            val backup = this.variableEnvironment
            variableEnvironment = newEnvironment(variableEnvironment)
            for ((i, formalParam) in formalParams.withIndex()) {
                variableEnvironment.bindings[formalParam] = values[i]
            }

            val result = interpret(body)
            variableEnvironment = backup
            return result
        }
        if (expression is LabeledCall) {
            val definition = functionEnvironment[expression.name]
                ?: throw KoyLangRuntimeException("Function ${expression.name} is not defined.")
            // Calling parameter map: label name -> param
            // e.g. f([x=1, y=2]) -> { x: 1, y: 2 }
            val labelMappings = expression.args.associate { it.name to it.parameter }

            val body = definition.body
            val formalParams = definition.args
            val actualParams = mutableListOf<Expression>()
            // Get actual parameters from labeled parameters map, so throw exception on failed to get value from mappings
            for (param in formalParams) {
                val e = labelMappings[param] ?: throw KoyLangRuntimeException("Parameter ${labelMappings[param]} is not defined in ${expression.name}")
                actualParams.add(e)
            }
            val values = actualParams.map { interpret(it) }

            // Called variable environment should be separated from Caller
            val backup = this.variableEnvironment
            variableEnvironment = newEnvironment(variableEnvironment)
            for ((i, formalParam) in formalParams.withIndex()) {
                variableEnvironment.bindings[formalParam] = values[i]
            }

            val result = interpret(body)
            variableEnvironment = backup
            return result
        }
        throw KoyLangRuntimeException("Expression $expression can not be parsed.")
    }

    private fun newEnvironment(next: Environment?): Environment = Environment(mutableMapOf(), next)

    /**
     * Execute `main` function. Throw runtime exception if a program has no `main` function.
     */
    fun callMain(program: Program): Value {
        val topLevels = program.definitions
        for (topLevel in topLevels) {
            when (topLevel) {
                is FunctionDefinition -> functionEnvironment[topLevel.name] = topLevel
                is GlobalVariableDefinition -> variableEnvironment.bindings[topLevel.name] = interpret(topLevel.expression)
            }
        }
        val mainFunction = functionEnvironment["main"]

        return if (mainFunction != null) {
            interpret(mainFunction.body)
        } else {
            throw KoyLangRuntimeException("This program should have main() function.")
        }
    }
}
