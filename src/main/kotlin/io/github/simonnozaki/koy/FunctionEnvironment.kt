package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.TopLevel.FunctionDefinition

data class FunctionEnvironment(
    // TODO consider renaming to letFunctions
    val bindings: MutableMap<String, FunctionDefinition> = mutableMapOf(),
    private val valFunctions: MutableMap<String, FunctionDefinition> = mutableMapOf()
) {
    /**
     * Set variable to environment as `val`
     */
    fun setAsVal(key: String, definition: FunctionDefinition) {
        valFunctions[key] = definition
    }

    /**
     * Set variable to environment as `mutable val`
     */
    fun setMutableVal(key: String, definition: FunctionDefinition) {
        if (hasDeclaration(key)) {
            throw KoyLangRuntimeException("Declaration [ $key ] is already existed, so can not declare again.")
        }
        bindings[key] = definition
    }

    /**
     * Return true if the identifier of `key` already exists.
     */
    private fun hasDeclaration(key: String) = bindings[key] != null || valFunctions[key] != null

    /**
     * Return function definition from `mutable val` or `val` declaration.
     */
    fun getDefinition(key: String): FunctionDefinition {
        val def = bindings[key]
        val valDef = valFunctions[key]
        if (valDef != null) {
            return valDef
        }
        if (def != null) {
            return def
        }
        throw KoyLangRuntimeException("Function [ $key ] is not defined.")
    }

    fun findBinding(key: String): FunctionDefinition {
        val def = bindings[key]
        val valDef = valFunctions[key]
        if (valDef != null) {
            return valDef
        }
        if (def != null) {
            return def
        }
        throw KoyLangRuntimeException("Definition [ $key ] is not defined.")
    }
}
