package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.TopLevel.FunctionDefinition

data class FunctionEnvironment(
    // TODO consider renaming to letFunctions
    private val bindings: MutableList<FunctionDefinition> = mutableListOf(),
    private val valFunctions: MutableList<FunctionDefinition> = mutableListOf()
) {

    /**
     * Set variable to environment as `val`
     */
    fun setAsVal(definition: FunctionDefinition) {
        valFunctions.add(definition)
    }

    /**
     * Set variable to environment as `mutable val`
     */
    fun setMutableVal(definition: FunctionDefinition) {
        if (hasDeclaration(definition.name)) {
            throw KoyLangRuntimeException("Declaration [ ${definition.name} ] is already existed, so can not declare again.")
        }
        bindings.add(definition)
    }

    /**
     * Return true if the identifier of `key` already exists.
     */
    private fun hasDeclaration(key: String): Boolean {
        return bindings.count { it.name == key } == 1 || valFunctions.count { it.name == key } == 1
    }

    /**
     * Return function definition from `mutable val` or `val` declaration.
     */
    fun getDefinition(key: String): FunctionDefinition {
        val def = bindings.firstOrNull { it.name == key }
        val valDef = valFunctions.firstOrNull { it.name == key }
        if (valDef != null) {
            return valDef
        }
        if (def != null) {
            return def
        }
        throw KoyLangRuntimeException("Function [ $key ] is not defined.")
    }
}
