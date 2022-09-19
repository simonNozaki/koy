package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Operator.*
import io.github.simonnozaki.koy.Expression.*

// TODO add a printer of tree node for debug logging
class Interpreter(
    private val functionEnvironment: MutableMap<String, FunctionDefinition> = mutableMapOf(),
    private var variableEnvironment: Environment = Environment(mutableMapOf(), null)
) {
    fun getValue(name: String) = variableEnvironment.bindings[name]

    fun interpret(expression: Expression): Value {
        if (expression is BinaryExpression) {
            val lhs = interpret(expression.lhs).asInt().value
            val rhs = interpret(expression.rhs).asInt().value

            val result =  when(expression.operator) {
                ADD -> lhs + rhs
                SUBTRACT -> lhs - rhs
                MULTIPLY -> lhs * rhs
                DIVIDE -> lhs / rhs
                LESS_THAN -> if (lhs < rhs) 1 else 0
                LESS_OR_EQUAL -> if (lhs <= rhs) 1 else 0
                GREATER_THAN -> if (lhs > rhs) 1 else 0
                GREATER_OR_EQUAL -> if (lhs >= rhs) 1 else 0
                EQUAL -> if (lhs == rhs) 1 else 0
                NOT_EQUAL -> if (lhs != rhs) 1 else 0
            }
            return Value.of(result)
        }
        if (expression is IntegerLiteral) {
            return Value.of(expression.value)
        }
        if (expression is ArrayLiteral) {
            val itemValues = expression.items.map { interpret(it) }
            return Value.of(itemValues)
        }
        if (expression is Identifier) {
            // Get variable
            val bindingOptions = variableEnvironment.findBindings(expression.name)
            val v = bindingOptions?.let { it[expression.name] } ?: throw RuntimeException("${expression.name} not found")

            return v
        }
        if (expression is Assignment) {
            // Assign variable
            val bindingOptions = variableEnvironment.findBindings(expression.name)
            val value = interpret(expression.expression)
            if (bindingOptions != null) {
                bindingOptions[expression.name] = value
            } else {
                variableEnvironment.bindings[expression.name] = value
            }
            return value
        }
        if (expression is PrintLn) {
            return interpret(expression.arg)
        }
        if (expression is IfExpression) {
            // TODO make condition as boolean
            val condition = interpret(expression.condition).asInt().value
            // 0 = false
            return if (condition != 0) {
                interpret(expression.thenClause)
            } else {
                expression.elseClause?.let { interpret(it) } ?: Value.of(1)
            }
        }
        if (expression is WhileExpression) {
            while (true) {
                val condition = interpret(expression.condition).asInt().value
                if (condition != 0) {
                    interpret(expression.body)
                } else {
                    break;
                }
            }
            return Value.of(1)
        }
        if (expression is BlockExpression) {
            // Block expression: interpret some expression from the top
            var v = 0
            val iterator = expression.elements.iterator()
            while (iterator.hasNext()) {
                v = interpret(iterator.next()).asInt().value
            }
            return  Value.of(v)
        }
        if (expression is FunctionCall) {
            val definition = functionEnvironment[expression.name] ?: throw RuntimeException("")

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
                ?: throw RuntimeException("Function ${expression.name} is not defined.")
            // Calling parameter map: label name -> param
            // e.g. f([x=1, y=2]) -> { x: 1, y: 2 }
            val labelMappings = expression.args.associate { it.name to it.parameter }

            val body = definition.body
            val formalParams = definition.args
            val actualParams = mutableListOf<Expression>()
            // Get actual parameters from labeled parameters map, so throw exception on failed to get value from mappings
            for (param in formalParams) {
                val e = labelMappings[param] ?: throw RuntimeException("Parameter ${labelMappings[param]} is not defined in ${expression.name}")
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
        throw RuntimeException("")
    }

    private fun newEnvironment(next: Environment?): Environment = Environment(mutableMapOf(), next)

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
            throw RuntimeException("This program should have main() function.")
        }
    }
}