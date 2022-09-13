package io.github.simonnozaki.koys

import io.github.simonnozaki.koys.Operator.ADD
import io.github.simonnozaki.koys.Operator.SUBTRACT
import io.github.simonnozaki.koys.Operator.MULTIPLY
import io.github.simonnozaki.koys.Operator.DIVIDE

// TODO add a printer of tree node for debug logging
class Interpreter(
    private val environment: MutableMap<String, Int> = mutableMapOf()
) {
    fun interpret(expression: Expression): Int {
        if (expression is Expression.BinaryExpression) {
            val lhs = interpret(expression.lhs)
            val rhs = interpret(expression.rhs)

            return when(expression.operator) {
                ADD -> lhs + rhs
                SUBTRACT -> lhs - rhs
                MULTIPLY -> lhs * rhs
                DIVIDE -> lhs / rhs
            }
        }
        if (expression is Expression.IntegerLiteral) {
            return expression.value
        }
        if (expression is Expression.Identifier) {
            // 変数の参照
            return environment[expression.value] ?: throw RuntimeException("")
        }
        if (expression is Expression.Assignment) {
            val v = this.interpret(expression.expression)
            this.environment[expression.name] = v
            return v
        }
        throw RuntimeException("")
    }
}