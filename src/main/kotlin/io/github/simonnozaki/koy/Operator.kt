package io.github.simonnozaki.koy

enum class Operator(
    val value: String
) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    EQUAL("=="),
    NOT_EQUAL("!=")
}
