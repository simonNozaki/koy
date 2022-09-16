package io.github.simonnozaki.koys

import io.github.simonnozaki.koys.Expression.*

fun add(lhs: Expression, rhs: Expression): BinaryExpression {
    return BinaryExpression(Operator.ADD, lhs, rhs)
}

fun subtract(lhs: Expression, rhs: Expression): BinaryExpression {
    return BinaryExpression(Operator.SUBTRACT, lhs, rhs)
}

fun multiply(lhs: Expression, rhs: Expression): BinaryExpression {
    return BinaryExpression(Operator.MULTIPLY, lhs, rhs)
}

fun divide(lhs: Expression, rhs: Expression): BinaryExpression {
    return BinaryExpression(Operator.DIVIDE, lhs, rhs)
}

fun lessThan(lhs: Expression, rhs: Expression) = BinaryExpression(Operator.LESS_THAN, lhs, rhs)

fun integer(value: Int): IntegerLiteral = IntegerLiteral(value)

fun identifier(name: String) = Identifier(name)

fun assign(name: String, expression: Expression) = Assignment(name, expression)

fun Block(vararg expressions: Expression): BlockExpression = BlockExpression(expressions.toList())

fun call(name: String, vararg expressions: Expression): FunctionCall = FunctionCall(name, expressions.toList())

fun Println(expression: Expression) = PrintLn(expression)

fun whileExpression(condition: Expression, body: Expression) = WhileExpression(condition, body)

fun defineFunction(name: String, args: List<String>, body: Expression) = FunctionDefinition(name, args, body)

fun If(
    condition: Expression,
    thenClause: Expression,
    elseClause: Expression?
): IfExpression = If(condition, thenClause, elseClause)

fun If(
    condition: Expression,
    thenClause: Expression
): IfExpression = If(condition, thenClause, null)

data class Program(
    val definitions: List<TopLevel>
)

data class Environment(
    val bindings: MutableMap<String, Int>,
    val next: Environment?
) {
    /**
     * Get values map from variable name along to `Environment` chaining
     * @param name
     */
    fun findBindings(name: String): MutableMap<String, Int>? {
        if (bindings[name] != null) {
            return bindings
        }
        if (next != null) {
            return next.findBindings(name)
        }
        return null
    }
}
