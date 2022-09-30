package io.github.simonnozaki.koy

data class VariableEnvironment(
    val bindings: MutableMap<String, Value>,
    val next: VariableEnvironment?
) {
    private val immutables: MutableMap<String, Value> = mutableMapOf()

    /**
     * Return true if the identifier of `key` already exists.
     */
    fun hasDeclaration(key: String): Boolean = findBindings(key) != null && immutables[key] != null

    fun setAsVal(key: String, value: Value) {
        immutables[key] = value
    }

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
