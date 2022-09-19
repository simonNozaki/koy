package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*

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

fun lessThanEqual(lhs: Expression, rhs: Expression) = BinaryExpression(Operator.LESS_OR_EQUAL, lhs, rhs)

fun greaterThan(lhs: Expression, rhs: Expression) = BinaryExpression(Operator.GREATER_THAN, lhs, rhs)

fun greaterThanEqual(lhs: Expression, rhs: Expression) = BinaryExpression(Operator.GREATER_OR_EQUAL, lhs, rhs)

fun equal(lhs: Expression, rhs: Expression) = BinaryExpression(Operator.EQUAL, lhs, rhs)

fun notEqual(lhs: Expression, rhs: Expression) = BinaryExpression(Operator.NOT_EQUAL, lhs, rhs)

fun integer(value: Int): IntegerLiteral = IntegerLiteral(value)

fun Array(vararg expressions: Expression) = ArrayLiteral(expressions.toList())

fun identifier(name: String) = Identifier(name)

fun assign(name: String, expression: Expression) = Assignment(name, expression)

fun Block(vararg expressions: Expression): BlockExpression = BlockExpression(expressions.toList())

fun call(name: String, vararg expressions: Expression): FunctionCall = FunctionCall(name, expressions.toList())

fun Println(expression: Expression) = PrintLn(expression)

fun defineFunction(name: String, args: List<String>, body: Expression) = FunctionDefinition(name, args, body)

fun If(
    condition: Expression,
    thenClause: Expression,
    elseClause: Expression?
): IfExpression = IfExpression(condition, thenClause, elseClause)

fun If(
    condition: Expression,
    thenClause: Expression
): IfExpression = IfExpression(condition, thenClause, null)

fun While(condition: Expression, body: Expression) = WhileExpression(condition, body)

data class Program(
    val definitions: List<TopLevel>
)

data class Environment(
    val bindings: MutableMap<String, Value>,
    val next: Environment?
) {
    /**
     * Get values map from variable name along to `Environment` chaining
     * @param name
     */
    fun findBindings(name: String): MutableMap<String, Value>? {
        if (bindings[name] != null) {
            return bindings
        }
        if (next != null) {
            return next.findBindings(name)
        }
        return null
    }
}
