package io.github.simonnozaki.koy

data class ObjectRuntimeEnvironment(
    val mutableValObjects: MutableMap<String, Map<String, Value>> = mutableMapOf(),
    private val valObjects: MutableMap<String, Map<String, Value>> = mutableMapOf()
) {
    fun setVal(key: String, properties: Map<String, Value>) {
        if (mutableValObjects[key] != null) {
            throw KoyLangRuntimeException("")
        }
        mutableValObjects[key] = properties
    }

    fun setMutableVal(key: String, properties: Map<String, Value>) {
        if (valObjects[key] != null) {
            throw KoyLangRuntimeException("")
        }
        valObjects[key] = properties
    }

    fun findBindings(key: String): MutableMap<String, Map<String, Value>>? {
        if (mutableValObjects[key] != null) {
            return mutableValObjects
        }
        if (valObjects[key] != null) {
            return valObjects
        }
        return null
    }

    fun isFunction(name: String, property: String): Boolean {
        val maybeMutableObject = mutableValObjects[name]
        val maybeObject = valObjects[name]
        if (maybeMutableObject != null && maybeMutableObject[property] != null) {
            if (maybeMutableObject[property] is Value.Function) {
                return true
            }
        }
        if (maybeObject != null && maybeObject[property] != null) {
            if (maybeObject[property] is Value.Function) {
                return true
            }
        }
        return false
    }
}