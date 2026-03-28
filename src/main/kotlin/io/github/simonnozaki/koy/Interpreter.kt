package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.ArrayLiteral
import io.github.simonnozaki.koy.Expression.Assignment
import io.github.simonnozaki.koy.Expression.BinaryExpression
import io.github.simonnozaki.koy.Expression.BlockExpression
import io.github.simonnozaki.koy.Expression.BoolLiteral
import io.github.simonnozaki.koy.Expression.FunctionCall
import io.github.simonnozaki.koy.Expression.FunctionLiteral
import io.github.simonnozaki.koy.Expression.Identifier
import io.github.simonnozaki.koy.Expression.IfExpression
import io.github.simonnozaki.koy.Expression.IndexAccess
import io.github.simonnozaki.koy.Expression.IntegerLiteral
import io.github.simonnozaki.koy.Expression.LabeledCall
import io.github.simonnozaki.koy.Expression.MethodCall
import io.github.simonnozaki.koy.Expression.MutableValDeclaration
import io.github.simonnozaki.koy.Expression.Nil
import io.github.simonnozaki.koy.Expression.ObjectLiteral
import io.github.simonnozaki.koy.Expression.PrintLn
import io.github.simonnozaki.koy.Expression.PushElement
import io.github.simonnozaki.koy.Expression.SetLiteral
import io.github.simonnozaki.koy.Expression.StringLiteral
import io.github.simonnozaki.koy.Expression.UnaryExpression
import io.github.simonnozaki.koy.Expression.ValDeclaration
import io.github.simonnozaki.koy.Expression.WhileExpression
import io.github.simonnozaki.koy.Operator.ADD
import io.github.simonnozaki.koy.Operator.DIVIDE
import io.github.simonnozaki.koy.Operator.EQUAL
import io.github.simonnozaki.koy.Operator.GREATER_OR_EQUAL
import io.github.simonnozaki.koy.Operator.GREATER_THAN
import io.github.simonnozaki.koy.Operator.LESS_OR_EQUAL
import io.github.simonnozaki.koy.Operator.LESS_THAN
import io.github.simonnozaki.koy.Operator.LOGICAL_AND
import io.github.simonnozaki.koy.Operator.LOGICAL_OR
import io.github.simonnozaki.koy.Operator.MULTIPLY
import io.github.simonnozaki.koy.Operator.NOT_EQUAL
import io.github.simonnozaki.koy.Operator.REMAINDER
import io.github.simonnozaki.koy.Operator.SUBTRACT
import io.github.simonnozaki.koy.TopLevel.FunctionDefinition
import io.github.simonnozaki.koy.TopLevel.MutableValDefinition
import io.github.simonnozaki.koy.TopLevel.ValDefinition
import io.github.simonnozaki.koy.UnaryOperator.DECREMENT
import io.github.simonnozaki.koy.UnaryOperator.INCREMENT
import java.io.PrintStream

// TODO builtin functions
class Interpreter(
    private val functionEnvironment: FunctionEnvironment = FunctionEnvironment(),
    private var variableEnvironment: VariableEnvironment = VariableEnvironment(mutableMapOf(), mutableMapOf(), null),
    private val objectRuntimeEnvironment: ObjectRuntimeEnvironment = ObjectRuntimeEnvironment(),
    private val out: PrintStream = System.out,
) {
    /**
     * Return the value from interpreter variable environment
     */
    fun getValue(name: String): Value? {
        val variableBindings = variableEnvironment.findBindings(name)
        if (variableBindings != null) {
            return variableBindings[name]
        }
        val objectBindings = objectRuntimeEnvironment.findBindings(name)
        if (objectBindings != null) {
            val v = objectBindings[name] ?: throw KoyLangRuntimeException("Not reaching here")
            return Value.ofObject(v)
        }
        return null
    }

    fun getFunction(name: String) = functionEnvironment.getDefinition(name)

    fun getFunctions() = functionEnvironment

    fun withDebug() = apply { this.requireDebugLog = true }

    private var requireDebugLog = false

    private fun requireBothInt(lhs: Value, rhs: Value, op: String) {
        if (lhs.isInt() && rhs.isInt()) return
        throw KoyLangRuntimeException("$lhs and $rhs must be integers on $op operation")
    }

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

            SUBTRACT -> {
                requireBothInt(lhs, rhs, "subtract")
                lhs.asInt().value - rhs.asInt().value
            }

            MULTIPLY -> {
                requireBothInt(lhs, rhs, "multiply")
                lhs.asInt().value * rhs.asInt().value
            }

            DIVIDE -> {
                requireBothInt(lhs, rhs, "divide")
                val right = rhs.asInt().value
                if (right == 0) throw KoyLangRuntimeException("Division by zero is not allowed")
                lhs.asInt().value / right
            }

            REMAINDER -> {
                requireBothInt(lhs, rhs, "remainder")
                val right = rhs.asInt().value
                if (right == 0) throw KoyLangRuntimeException("Division by zero is not allowed")
                lhs.asInt().value % right
            }

            LESS_THAN -> {
                requireBothInt(lhs, rhs, "less-than")
                lhs.asInt().value < rhs.asInt().value
            }

            LESS_OR_EQUAL -> {
                requireBothInt(lhs, rhs, "less-or-equal")
                lhs.asInt().value <= rhs.asInt().value
            }

            GREATER_THAN -> {
                requireBothInt(lhs, rhs, "greater-than")
                lhs.asInt().value > rhs.asInt().value
            }

            GREATER_OR_EQUAL -> {
                requireBothInt(lhs, rhs, "greater-or-equal")
                lhs.asInt().value >= rhs.asInt().value
            }

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

    fun interpret(expression: Expression): Value =
        if (requireDebugLog) {
            out.println("|- $expression")
            execute(expression)
        } else {
            execute(expression)
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

                else -> {
                    variableEnvironment.setVal(expression.name, value)
                }
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

                else -> {
                    variableEnvironment.setMutableVal(expression.name, value)
                }
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
                val m = "Declaration [ ${expression.name} ] is declared as val, so consider declaring it as mutable val declaration."
                throw KoyLangRuntimeException(m)
            }
            when (value) {
                is Value.Object -> maybeObject?.set(expression.name, value.value)
                else -> bindingOptions?.set(expression.name, value)
            }
            return value
        }
        if (expression is PrintLn) {
            val v = interpret(expression.arg)
            out.println(v)
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
            } catch (_: Exception) {
                Value.Nil
            }
        }
        if (expression is PushElement) {
            val struct = interpret(expression.struct)
            val elm = interpret(expression.element)
            return when (struct) {
                is Value.Array -> {
                    val mutableItems = struct.items.toMutableList()
                    mutableItems.add(elm)
                    Value.Array(mutableItems.toList(), mutableItems.size)
                }

                is Value.Set -> {
                    val mutableSet = struct.value.toMutableSet()
                    mutableSet.add(elm)
                    Value.Set(mutableSet.toSet(), mutableSet.size)
                }

                else -> {
                    throw KoyLangRuntimeException("Subject $struct is not pushable.")
                }
            }
        }
        if (expression is FunctionCall) {
            val definition = functionEnvironment.getDefinition(expression.name)

            val actualParams = expression.args
            val formalParams = definition.args
            val body = definition.body

            if (actualParams.size != formalParams.size) {
                val m = "Function ${expression.name} expects ${formalParams.size} argument(s) but got ${actualParams.size}"
                throw KoyLangRuntimeException(m)
            }
            val values = actualParams.map { interpret(it) }

            // Called variable environment should be separated from Caller
            val backup = this.variableEnvironment
            variableEnvironment = newEnvironment(variableEnvironment)
            for ((i, formalParam) in formalParams.withIndex()) {
                variableEnvironment.mutableVals[formalParam] = values[i]
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
                val m = "Parameter ${labelMappings[param]} is not defined in ${expression.name}"
                val e = labelMappings[param] ?: throw KoyLangRuntimeException(m)
                actualParams.add(e)
            }
            val values = actualParams.map { interpret(it) }

            // Called variable environment should be separated from Caller
            val backup = this.variableEnvironment
            variableEnvironment = newEnvironment(variableEnvironment)
            for ((i, formalParam) in formalParams.withIndex()) {
                variableEnvironment.mutableVals[formalParam] = values[i]
            }

            val result = interpret(body)
            variableEnvironment = backup
            return result
        }
        if (expression is MethodCall) {
            val obj: Map<String, Value> =
                when (expression.objectExpression) {
                    is ObjectLiteral -> {
                        // On calling method from object literal, create map
                        expression.objectExpression.properties.entries
                            .associate { it.key to interpret(it.value) }
                    }

                    is Identifier -> {
                        // On calling method from object variable, get property name and value map from runtime
                        objectRuntimeEnvironment
                            .findBindings(expression.objectExpression.name)
                            ?.get(expression.objectExpression.name)
                            ?: throw KoyLangRuntimeException("Object [ ${expression.objectExpression} ] is not defined.")
                    }

                    else -> {
                        return interpret(expression.objectExpression)
                    }
                }
            if (expression.method !is Identifier) {
                throw KoyLangRuntimeException("Method [ ${expression.method} ] should be identifier.")
            }

            val m = "Object [ ${expression.objectExpression} ] does not have a method or property [ ${expression.method} ]."
            val method = obj[expression.method.name] ?: throw KoyLangRuntimeException(m)
            if (method !is Value.Function) {
                return method
            }
            val body = method.body
            val actualParams = expression.args
            val formalParams = method.args
            if (actualParams.size != formalParams.size) {
                throw KoyLangRuntimeException(
                    "Method ${expression.method} expects ${formalParams.size} argument(s) but got ${actualParams.size}",
                )
            }
            val values = actualParams.map { interpret(it) }

            val backup = variableEnvironment
            variableEnvironment = newEnvironment(variableEnvironment)
            for ((i, formalParam) in formalParams.withIndex()) {
                variableEnvironment.mutableVals[formalParam] = values[i]
            }
            val result = interpret(body)
            variableEnvironment = backup
            return result
        }
        throw KoyLangRuntimeException("Expression $expression can not be parsed.")
    }

    private fun newEnvironment(next: VariableEnvironment?): VariableEnvironment = VariableEnvironment(mutableMapOf(), mutableMapOf(), next)

    /**
     * Execute `main` function. Throw runtime exception if a program has no `main` function.
     */
    fun callMain(program: Program): Value {
        val topLevels = program.definitions
        for (topLevel in topLevels) {
            when (topLevel) {
                is FunctionDefinition -> {
                    functionEnvironment.setAsVal(topLevel)
                }

                is ValDefinition -> {
                    when (topLevel.expression) {
                        is FunctionLiteral -> {
                            val def = defineFunction(topLevel.name, topLevel.expression.args, topLevel.expression.body)
                            functionEnvironment.setAsVal(def)
                        }

                        is ObjectLiteral -> {
                            val o = topLevel.expression.properties.entries
                                .associate { it.key to interpret(it.value) }
                            objectRuntimeEnvironment.setVal(topLevel.name, o)
                        }

                        else -> {
                            variableEnvironment.setVal(topLevel.name, interpret(topLevel.expression))
                        }
                    }
                }

                is MutableValDefinition -> {
                    when (topLevel.expression) {
                        is FunctionLiteral -> {
                            val def = defineFunction(topLevel.name, topLevel.expression.args, topLevel.expression.body)
                            functionEnvironment.setMutableVal(def)
                        }

                        is ObjectLiteral -> {
                            val o = topLevel.expression.properties.entries
                                .associate { it.key to interpret(it.value) }
                            objectRuntimeEnvironment.setMutableVal(topLevel.name, o)
                        }

                        else -> {
                            variableEnvironment.setMutableVal(topLevel.name, interpret(topLevel.expression))
                        }
                    }
                }
            }
        }
        val mainFunction = functionEnvironment.getDefinition("main")

        return interpret(mainFunction.body)
    }
}
