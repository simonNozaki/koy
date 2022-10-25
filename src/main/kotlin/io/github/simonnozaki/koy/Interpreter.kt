package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import io.github.simonnozaki.koy.Operator.*
import io.github.simonnozaki.koy.TopLevel.FunctionDefinition
import io.github.simonnozaki.koy.TopLevel.ValDefinition
import io.github.simonnozaki.koy.TopLevel.MutableValDefinition
import io.github.simonnozaki.koy.UnaryOperator.INCREMENT
import io.github.simonnozaki.koy.UnaryOperator.DECREMENT

// TODO builtin functions
class Interpreter(
    private val functionEnvironment: FunctionEnvironment = FunctionEnvironment(),
    private var variableEnvironment: VariableEnvironment = VariableEnvironment(mutableMapOf(), null),
    private val objectRuntimeEnvironment: ObjectRuntimeEnvironment = ObjectRuntimeEnvironment()
) {
    /**
     * Return the value from interpreter variable environment
     */
    fun getValue(name: String): Value? {
        if (variableEnvironment.findBindings(name) != null) {
            return variableEnvironment.findBindings(name)?.get(name)
        }
        if (objectRuntimeEnvironment.findBindings(name) != null) {
            val v = objectRuntimeEnvironment.findBindings(name)?.get(name) ?: throw KoyLangRuntimeException("Not reaching here")
            return Value.ofObject(v)
        }
        return null
    }

    fun getFunction(name: String) = functionEnvironment.getDefinition(name)

    fun getFunctions() = functionEnvironment

    /**
     * Return all variables and nodes of syntax tree from environment.
     */
    fun getVariables() = variableEnvironment.bindings.toMap()

    fun withDebug() = apply {
        this.requireDebugLog = true
    }

    private var requireDebugLog = false

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
            REMAINDER -> lhs.asInt().value % rhs.asInt().value
            LESS_THAN -> lhs.asInt().value < rhs.asInt().value
            LESS_OR_EQUAL -> lhs.asInt().value <= rhs.asInt().value
            GREATER_THAN -> lhs.asInt().value > rhs.asInt().value
            GREATER_OR_EQUAL -> lhs.asInt().value >= rhs.asInt().value
            EQUAL -> {
                if (lhs.isInt() && rhs.isInt()) {
                    lhs.asInt().value == rhs.asInt().value
                } else if (lhs.isBool() && rhs.isBool()) {
                    lhs.asBool().value == rhs.asBool().value
                } else if (lhs.isString() && rhs.isString()) {
                    lhs.asString().value == rhs.asString().value
                } else if (lhs.isSet() && rhs.isSet()) {
                    lhs.asSet().value == rhs.asSet().value
                } else if (lhs.isArray() && rhs.isArray()) {
                    lhs.asArray().items == rhs.asArray().items
                } else if (lhs.isNil() && rhs.isNil()) {
                    true
                } else if (lhs.isNil() || rhs.isNil()) {
                    false
                } else {
                    throw KoyLangRuntimeException("$lhs and $rhs is not comparable.")
                }
            }
            NOT_EQUAL -> {
                if (lhs.isInt() && rhs.isInt()) {
                    lhs.asInt().value != rhs.asInt().value
                } else if (lhs.isBool() && rhs.isBool()) {
                    lhs.asBool().value != rhs.asBool().value
                } else if (lhs.isString() && rhs.isString()) {
                    lhs.asString().value != rhs.asString().value
                } else if (lhs.isSet() && rhs.isSet()) {
                    lhs.asSet().value != rhs.asSet().value
                } else if (lhs.isArray() && rhs.isArray()) {
                    lhs.asArray().items != rhs.asArray().items
                } else if (lhs.isNil() && rhs.isNil()) {
                    false
                } else if (lhs.isNil() || rhs.isNil()) {
                    true
                } else {
                    throw KoyLangRuntimeException("$lhs and $rhs is not comparable.")
                }
            }
            LOGICAL_AND -> {
                if (lhs.isBool() && rhs.isBool()) {
                    lhs.asBool().value && rhs.asBool().value
                } else {
                    throw KoyLangRuntimeException("Both $lhs and $rhs should be boolean.")
                }
            }
            LOGICAL_OR -> {
                if (lhs.isBool() && rhs.isBool()) {
                    lhs.asBool().value || rhs.asBool().value
                } else {
                    throw KoyLangRuntimeException("Both $lhs and $rhs should be boolean.")
                }
            }
        }
        return Value.of(result)
    }

    fun interpret(expression: Expression): Value {
        return if (requireDebugLog) {
            println("|- $expression")
            execute(expression)
        } else {
            execute(expression)
        }
    }

    private fun execute(expression: Expression): Value {
        if (expression is UnaryExpression) {
            if (expression.value !is Identifier) {
                throw KoyLangRuntimeException("Unary operation needs variable.")
            }

            val identifier = try {
                interpret(expression.value).asInt().value
            } catch (e: Exception) {
                throw KoyLangRuntimeException("Unary operation should apply with integer variables, error => $e")
            }

            val v = when (expression.operator) {
                INCREMENT -> identifier + 1
                DECREMENT -> identifier - 1
            }
            interpret(assign(expression.value.name, IntegerLiteral(v)))
            return Value.of(v)
        }
        if (expression is BinaryExpression) {
            return getBinaryOpsResult(expression)
        }
        if (expression is Nil) {
            return Value.Nil
        }
        if (expression is IntegerLiteral) {
            return Value.of(expression.value)
        }
        if (expression is ArrayLiteral) {
            val itemValues = expression.items.map { interpret(it) }
            return Value.of(itemValues)
        }
        if (expression is SetLiteral) {
            val elms: Set<Value> = expression.value.map { interpret((it)) }.toSet()
            return Value.of(elms)
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
            return getValue(expression.name) ?: throw KoyLangRuntimeException("Identifier ${expression.name} not found")
        }
        if (expression is ValDeclaration) {
            val value = interpret(expression.expression)
            when (value) {
                is Value.Function -> {
                    val def = defineFunction(expression.name, value.args, value.body)
                    functionEnvironment.setAsVal(def)
                }
                is Value.Object -> {
                    objectRuntimeEnvironment.setVal(expression.name, value.value)
                }
                else -> variableEnvironment.setVal(expression.name, value)
            }
            return value
        }
        if (expression is MutableValDeclaration) {
            val value = interpret(expression.expression)
            when (value) {
                is Value.Function -> {
                    val def = defineFunction(expression.name, value.args, value.body)
                    functionEnvironment.setMutableVal(def)
                }
                is Value.Object -> {
                    objectRuntimeEnvironment.setMutableVal(expression.name, value.value)
                }
                else -> variableEnvironment.setMutableVal(expression.name, value)
            }
            return value
        }
        if (expression is Assignment) {
            // Assign variable
            val bindingOptions = variableEnvironment.findBindings(expression.name)
            val maybeObject = objectRuntimeEnvironment.findBindings(expression.name)
            val value = interpret(expression.expression)
            if (bindingOptions == null && maybeObject == null) {
                throw KoyLangRuntimeException("Declaration [ ${expression.name} ] is not defined.")
            }
            if (variableEnvironment.isNotReAssignable(expression.name)) {
                throw KoyLangRuntimeException("Declaration [ ${expression.name} ] is declared as val, so consider declaring it as mutable val declaration.")
            }
            when (value) {
                is Value.Object -> maybeObject?.set(expression.name, value.value)
                else -> bindingOptions?.set(expression.name, value)
            }
            return value
        }
        if (expression is PrintLn) {
            val v = interpret(expression.arg)
            println(v)
            return v
        }
        if (expression is IfExpression) {
            val condition = interpret(expression.condition).asBool().value
            return if (condition) {
                interpret(expression.thenClause)
            } else {
                expression.elseClause.map { interpret(it) }.orElse(Value.of(true))
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
        if (expression is IndexAccess) {
            return try {
                val index = interpret(expression.index).asInt()
                val collection = interpret(expression.collection).asArray()
                collection.items[index.value]
            } catch (e: Exception) {
                Value.Nil
            }
        }
        if (expression is PushElement) {
            val struct = interpret(expression.struct)
            val elm = interpret(expression.element)
            when (struct) {
                is Value.Array -> {
                    val mutableItems = struct.items.toMutableList()
                    mutableItems.add(elm)
                    Value.Array(mutableItems.toList(), mutableItems.size)
                }
                is Value.Set -> {
                    val mutableSet = struct.value.toMutableSet()
                    mutableSet.add(elm)
                    Value.Set(mutableSet, mutableSet.size)
                }
                else -> throw KoyLangRuntimeException("Subject $struct is not pushable.")
            }
        }
        if (expression is FunctionCall) {
            val definition = functionEnvironment.getDefinition(expression.name)

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
            val definition = functionEnvironment.getDefinition(expression.name)
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
        if (expression is MethodCall) {
            val obj: Map<String, Value> = when (expression.objectExpression) {
                is ObjectLiteral -> {
                    // On calling method from object literal, create map
                    expression.objectExpression.properties.entries.associate { it.key to interpret(it.value) }
                }
                is Identifier -> {
                    // On calling method from object variable, get property name and value map from runtime
                    objectRuntimeEnvironment.findBindings(expression.objectExpression.name)
                        ?.get(expression.objectExpression.name)
                        ?: throw KoyLangRuntimeException("Object [ ${expression.objectExpression} ] is not defined.")
                }
                else -> return interpret(expression.objectExpression)
            }
            if (expression.method !is Identifier) {
                throw KoyLangRuntimeException("Method [ ${expression.method} ] should be identifier.")
            }

            val method = obj[expression.method.name]
                ?: throw KoyLangRuntimeException("Object [ ${expression.objectExpression} ] does not have a method or property [ ${expression.method} ].")
            if (method !is Value.Function) {
                return method
            }
            val body = method.body
            val actualParams = expression.args
            val formalParams = method.args
            val values = actualParams.map { interpret(it) }

            val backup = variableEnvironment
            variableEnvironment = newEnvironment(variableEnvironment)
            // TODO check parameter number and type before loop
            for ((i, formalParam) in formalParams.withIndex()) {
                variableEnvironment.bindings[formalParam] = values[i]
            }
            val result = interpret(body)
            variableEnvironment = backup
            return result
        }
        throw KoyLangRuntimeException("Expression $expression can not be parsed.")
    }

    private fun newEnvironment(next: VariableEnvironment?): VariableEnvironment = VariableEnvironment(mutableMapOf(), next)

    /**
     * Execute `main` function. Throw runtime exception if a program has no `main` function.
     */
    fun callMain(program: Program): Value {
        val topLevels = program.definitions
        for (topLevel in topLevels) {
            when (topLevel) {
                is FunctionDefinition -> functionEnvironment.setAsVal(topLevel)
                is ValDefinition -> {
                    when (topLevel.expression) {
                        is FunctionLiteral -> {
                            val def = defineFunction(topLevel.name, topLevel.expression.args, topLevel.expression.body)
                            functionEnvironment.setAsVal(def)
                        }
                        is ObjectLiteral -> {
                            val o = topLevel.expression.properties.entries.associate { it.key to interpret(it.value) }
                            objectRuntimeEnvironment.setVal(topLevel.name, o)
                        }
                        else -> variableEnvironment.setVal(topLevel.name, interpret(topLevel.expression))
                    }
                }
                is MutableValDefinition -> {
                    when (topLevel.expression) {
                        is FunctionLiteral -> {
                            val def = defineFunction(topLevel.name, topLevel.expression.args, topLevel.expression.body)
                            functionEnvironment.setMutableVal(def)
                        }
                        is ObjectLiteral -> {
                            val o = topLevel.expression.properties.entries.associate { it.key to interpret(it.value) }
                            objectRuntimeEnvironment.setMutableVal(topLevel.name, o)
                        }
                        else -> variableEnvironment.setMutableVal(topLevel.name, interpret(topLevel.expression))
                    }
                }
            }
        }
        val mainFunction = functionEnvironment.getDefinition("main")

        return interpret(mainFunction.body)
    }
}
