package io.github.simonnozaki.koy

import java.util.Optional

sealed class Expression {
    data class IntegerLiteral(
        val value: Int
    ) : Expression()

    data class ArrayLiteral(
        val items: List<Expression>
    ) : Expression()

    data class BoolLiteral(
        val value: Boolean
    ) : Expression()

    data class StringLiteral(
        val value: String
    ) : Expression()

    data class ObjectLiteral(
        val properties: Map<String, Expression>
    ) : Expression()

    data class FunctionLiteral(
        val args: List<String>,
        val body: BlockExpression
    ) : Expression()

    data class SetLiteral(
        val value: Set<Expression>
    ) : Expression()

    object Nil : Expression()

    data class BinaryExpression(
        val operator: Operator,
        val lhs: Expression,
        val rhs: Expression
    ) : Expression()

    data class UnaryExpression(
        val operator: UnaryOperator,
        val value: Expression
    ) : Expression()

    data class Assignment(
        val name: String,
        val expression: Expression
    ) : Expression()

    data class ValDeclaration(
        val name: String,
        val expression: Expression
    ) : Expression()

    data class MutableValDeclaration(
        val name: String,
        val expression: Expression
    ) : Expression()

    data class Identifier(
        val name: String
    ) : Expression()

    data class IfExpression(
        val condition: Expression,
        val thenClause: Expression,
        val elseClause: Optional<Expression>
    ) : Expression()

    data class BlockExpression(
        val elements: List<Expression>
    ) : Expression()

    data class WhileExpression(
        val condition: Expression,
        val body: Expression
    ) : Expression()

    data class FunctionCall(
        val name: String,
        val args: List<Expression>
    ) : Expression()

    data class MethodCall(
        val objectExpression: Expression,
        val method: Expression,
        val args: List<Expression>
    ) : Expression()

    data class IndexAccess(
        val collection: Expression,
        val index: Expression
    ) : Expression()

    data class PushElement(
        val struct: Expression,
        val element: Expression
    ) : Expression()

    data class LabeledParameter(
        val name: String,
        val parameter: Expression
    ) : Expression()

    data class LabeledCall(
        val name: String,
        val args: List<LabeledParameter>
    ) : Expression()

    data class PrintLn(
        val arg: Expression
    ) : Expression()
}
