package io.github.simonnozaki.koy

data class RuntimeEnvironment(
    val bindings: MutableMap<String, Value>,
    val next: RuntimeEnvironment?
) {
    private val immutables: MutableMap<String, Value> = mutableMapOf()

    fun hasDeclaration(key: String): Boolean = bindings[key] != null && immutables[key] != null

    /**
     * Return true if a value related to the `key` is immutable
     */
    fun isNotReassignable(key: String): Boolean = immutables[key] != null

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
