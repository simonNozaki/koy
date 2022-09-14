package io.github.simonnozaki.koys

import io.github.simonnozaki.koys.Operator.*
import io.github.simonnozaki.koys.Expression.*

// TODO add a printer of tree node for debug logging
class Interpreter(
    private val environment: MutableMap<String, Int> = mutableMapOf(),
    private val functionEnvironment: MutableMap<String, FunctionDefinition> = mutableMapOf(),
    private var variableEnvironment: Environment
) {
    fun interpret(expression: Expression): Int {
        if (expression is BinaryExpression) {
            val lhs = interpret(expression.lhs)
            val rhs = interpret(expression.rhs)

            return when(expression.operator) {
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
        }
        if (expression is IntegerLiteral) {
            return expression.value
        }
        if (expression is Expression.Identifier) {
            // Get variable
            return environment[expression.value] ?: throw RuntimeException("")
        }
        if (expression is Assignment) {
            // Assign variable
            val v = this.interpret(expression.expression)
            this.environment[expression.name] = v
            return v
        }
        if (expression is IfExpression) {
            val condition = interpret(expression.condition)
            // 0 = false
            return if (condition != 0) {
                interpret(expression.thenClause)
            } else {
                expression.elseClause?.let { interpret(it) } ?: 1
            }
        }
        if (expression is WhileExpression) {
            while (true) {
                val condition = interpret(expression.condition)
                return if (condition != 0) {
                    interpret(expression.body)
                } else {
                    break;
                }
            }
        }
        if (expression is BlockExpression) {
            // Block expression: interpret some expression from the top
            var v = 0
            val iterator = expression.elements.iterator()
            while (iterator.hasNext()) {
                v = interpret(iterator.next())
            }
            return  v
        }
        if (expression is FunctionCall) {
            val definition = functionEnvironment[expression.name] ?: throw RuntimeException("")

            val actualParams = expression.args
            val formalParams = definition.args
            val body = definition.body

            val values = actualParams.map { interpret(it) }
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

    private fun callMain(program: Program): Int {
        val topLevels = program.definitions
        for (topLevel in topLevels) {
            if (topLevel is FunctionDefinition) {
                functionEnvironment[topLevel.name] = topLevel
            } else {
                // グローバル変数対応
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