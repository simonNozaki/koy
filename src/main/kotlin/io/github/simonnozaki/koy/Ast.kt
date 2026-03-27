package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.ArrayLiteral
import io.github.simonnozaki.koy.Expression.Assignment
import io.github.simonnozaki.koy.Expression.BinaryExpression
import io.github.simonnozaki.koy.Expression.BlockExpression
import io.github.simonnozaki.koy.Expression.BoolLiteral
import io.github.simonnozaki.koy.Expression.FunctionCall
import io.github.simonnozaki.koy.Expression.Identifier
import io.github.simonnozaki.koy.Expression.IfExpression
import io.github.simonnozaki.koy.Expression.IntegerLiteral
import io.github.simonnozaki.koy.Expression.ObjectLiteral
import io.github.simonnozaki.koy.Expression.PrintLn
import io.github.simonnozaki.koy.Expression.StringLiteral
import io.github.simonnozaki.koy.Expression.UnaryExpression
import io.github.simonnozaki.koy.Expression.WhileExpression
import io.github.simonnozaki.koy.TopLevel.FunctionDefinition
import java.util.Optional

fun add(
    lhs: Expression,
    rhs: Expression,
): BinaryExpression {
    return BinaryExpression(Operator.ADD, lhs, rhs)
}

fun subtract(
    lhs: Expression,
    rhs: Expression,
): BinaryExpression {
    return BinaryExpression(Operator.SUBTRACT, lhs, rhs)
}

fun multiply(
    lhs: Expression,
    rhs: Expression,
): BinaryExpression {
    return BinaryExpression(Operator.MULTIPLY, lhs, rhs)
}

fun divide(
    lhs: Expression,
    rhs: Expression,
): BinaryExpression {
    return BinaryExpression(Operator.DIVIDE, lhs, rhs)
}

fun remain(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.REMAINDER, lhs, rhs)

fun lessThan(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.LESS_THAN, lhs, rhs)

fun lessThanEqual(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.LESS_OR_EQUAL, lhs, rhs)

fun greaterThan(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.GREATER_THAN, lhs, rhs)

fun greaterThanEqual(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.GREATER_OR_EQUAL, lhs, rhs)

fun equal(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.EQUAL, lhs, rhs)

fun notEqual(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.NOT_EQUAL, lhs, rhs)

fun logicalAnd(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.LOGICAL_AND, lhs, rhs)

fun logicalOr(
    lhs: Expression,
    rhs: Expression,
) = BinaryExpression(Operator.LOGICAL_OR, lhs, rhs)

fun integer(value: Int): IntegerLiteral = IntegerLiteral(value)

fun bool(value: Boolean) = BoolLiteral(value)

@Suppress("ktlint:standard:function-naming")
fun Array(vararg expressions: Expression) = ArrayLiteral(expressions.toList())

@Suppress("ktlint:standard:function-naming")
fun Object(properties: Map<String, Expression>) = ObjectLiteral(properties)

fun str(value: String) = StringLiteral(value)

fun identifier(name: String) = Identifier(name)

fun assign(
    name: String,
    expression: Expression,
) = Assignment(name, expression)

@Suppress("ktlint:standard:function-naming")
fun Block(vararg expressions: Expression): BlockExpression = BlockExpression(expressions.toList())

fun call(
    name: String,
    vararg expressions: Expression,
): FunctionCall = FunctionCall(name, expressions.toList())

@Suppress("ktlint:standard:function-naming")
fun Println(expression: Expression) = PrintLn(expression)

fun defineFunction(
    name: String,
    args: List<String>,
    body: Expression,
) = FunctionDefinition(name, args, body)

fun increment(name: String) = UnaryExpression(UnaryOperator.INCREMENT, identifier(name))

fun decrement(name: String) = UnaryExpression(UnaryOperator.DECREMENT, identifier(name))

@Suppress("ktlint:standard:function-naming")
fun If(
    condition: Expression,
    thenClause: Expression,
    elseClause: Optional<Expression>,
): IfExpression = IfExpression(condition, thenClause, elseClause)

@Suppress("ktlint:standard:function-naming")
fun While(
    condition: Expression,
    body: Expression,
) = WhileExpression(condition, body)

data class Program(
    val definitions: List<TopLevel>,
)
