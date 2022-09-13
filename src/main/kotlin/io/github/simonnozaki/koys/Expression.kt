package io.github.simonnozaki.koys

sealed class Expression {
    data class IntegerLiteral(
        val value: Int
    ) : Expression()

    data class BinaryExpression(
        val operator: Operator,
        val lhs: Expression,
        val rhs: Expression
    ) : Expression()

    data class Assignment(
        val name: String,
        val expression: Expression
    ) : Expression()

    data class Identifier(
        val value: String
    ) : Expression()

    data class IfExpression(
        val condition: Expression,
        val thenClause: Expression,
        val elseClause: Expression?
    ) : Expression()

    data class BlockExpression(
        val elements: List<Expression>
    ) : Expression()

    data class WhileExpression(
        val condition: Expression,
        val body: Expression
    ) : Expression()
}
