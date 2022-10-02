package io.github.simonnozaki.koy

sealed class TopLevel {
    data class FunctionDefinition(
        val name: String,
        val args: List<String>,
        val body: Expression
    ) : TopLevel()

    data class ValDefinition(
        val name: String,
        val expression: Expression
    ) : TopLevel()

    data class MutableValDefinition(
        val name: String,
        val expression: Expression
    ) : TopLevel()
}
