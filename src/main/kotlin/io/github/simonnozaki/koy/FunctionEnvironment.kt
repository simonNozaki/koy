package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.TopLevel.FunctionDefinition

/**
 * Environment that has function definitions in runtime
 */
data class FunctionEnvironment(
    // TODO consider renaming to letFunctions
    private val mutableFunctions: MutableMap<String, FunctionDefinition> = mutableMapOf(),
    private val valFunctions: MutableMap<String, FunctionDefinition> = mutableMapOf(),
) {
    /**
     * Set variable to environment as `val`
     */
    fun setAsVal(definition: FunctionDefinition) {
        throwIfDefinitionExists(definition.name)
        valFunctions[definition.name] = definition
    }

    /**
     * Set variable to environment as `mutable val`
     */
    fun setMutableVal(definition: FunctionDefinition) {
        throwIfDefinitionExists(definition.name)
        mutableFunctions[definition.name] = definition
    }

    private fun throwIfDefinitionExists(name: String) {
        if (mutableFunctions.containsKey(name) || valFunctions.containsKey(name)) {
            throw KoyLangRuntimeException("Declaration [ $name ] is already existed, so can not declare again.")
        }
    }

    /**
     * Return function definition from `mutable val` or `val` declaration.
     */
    fun getDefinition(key: String): FunctionDefinition {
        val def = mutableFunctions[key]
        val valDef = valFunctions[key]
        if (valDef != null) {
            return valDef
        }
        if (def != null) {
            return def
        }
        throw KoyLangRuntimeException("Function [ $key ] is not defined.")
    }
}
