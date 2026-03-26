package io.github.simonnozaki.koy

/**
 * Environment that has variable definitions in runtime
 */
data class VariableEnvironment(
    val mutableVals: MutableMap<String, Value>,
    private val immutablesVals: MutableMap<String, Value> = mutableMapOf(),
    private val next: VariableEnvironment?
) {

    /**
     * Guard not to add duplicated variable entry
     */
    private fun throwIfDefinitionExists(name: String) {
        if (mutableVals[name] != null || immutablesVals[name] != null) {
            throw KoyLangRuntimeException("Declaration [ $name ] is already existed, so can not declare again.")
        }
    }

    /**
     * Set variable to environment as `val`
     */
    fun setVal(key: String, value: Value) {
        throwIfDefinitionExists(key)
        immutablesVals[key] = value
    }

    /**
     * Set variable to environment as `mutable val`
     */
    fun setMutableVal(key: String, value: Value) {
        throwIfDefinitionExists(key)
        mutableVals[key] = value
    }

    /**
     * Return true if there is a variable of `key`.
     */
    fun isNotReAssignable(key: String): Boolean = immutablesVals[key] != null

    /**
     * Get values map from variable name along to `Environment` chaining
     * @param name
     */
    fun findBindings(name: String): MutableMap<String, Value>? {
        if (mutableVals[name] != null) {
            return mutableVals
        }
        if (immutablesVals[name] != null) {
            return immutablesVals
        }
        if (next != null) {
            return next.findBindings(name)
        }
        return null
    }
}
