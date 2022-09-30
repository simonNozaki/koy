package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.TopLevel.FunctionDefinition

data class FunctionEnvironment(
    // TODO consider renaming to letFunctions
    val bindings: MutableMap<String, FunctionDefinition> = mutableMapOf(),
    private val valFunctions: MutableMap<String, FunctionDefinition> = mutableMapOf()
) {
    fun setAsVal(key: String, definition: FunctionDefinition) {
        valFunctions[key] = definition
    }

    fun hasDeclaration(key: String) = bindings[key] != null && valFunctions[key] != null

    fun getDefinition(key: String): FunctionDefinition {
        val def = bindings[key]
        val valDef = valFunctions[key]
        if (def != null) {
            return def
        }
        if (valDef != null) {
            return valDef
        }
        throw KoyLangRuntimeException("Function [ $key ] not found")
    }
}
