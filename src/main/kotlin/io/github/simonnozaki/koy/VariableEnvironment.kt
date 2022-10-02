package io.github.simonnozaki.koy

data class VariableEnvironment(
    val bindings: MutableMap<String, Value>,
    val next: VariableEnvironment?,
    private val immutables: MutableMap<String, Value> = mutableMapOf()
) {

    /**
     * Return true if the identifier of `key` already exists.
     */
    private fun hasDeclaration(key: String): Boolean = findBindings(key) != null

    /**
     * Set variable to environment as `val`
     */
    fun setVal(key: String, value: Value) {
        if (findBindings(key) != null) {
            throw KoyLangRuntimeException("Declaration [ $key ] is already existed, so can not declare again.")
        }
        immutables[key] = value
    }

    /**
     * Set variable to environment as `mutable val`
     */
    fun setMutableVal(key: String, value: Value) {
        if (hasDeclaration(key)) {
            throw KoyLangRuntimeException("Declaration [ $key ] is already existed, so can not declare again.")
        }
        bindings[key] = value
    }

    /**
     * Return true if there is a variable of `key`.
     */
    fun isNotReAssignable(key: String): Boolean = immutables[key] != null

    /**
     * Get values map from variable name along to `Environment` chaining
     * @param name
     */
    fun findBindings(name: String): MutableMap<String, Value>? {
        if (bindings[name] != null) {
            return bindings
        }
        if (immutables[name] != null) {
            return immutables
        }
        if (next != null) {
            return next.findBindings(name)
        }
        return null
    }
}
