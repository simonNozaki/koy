package io.github.simonnozaki.koy

data class ObjectRuntimeEnvironment(
    private val mutableValObjects: MutableMap<String, Map<String, Value>> = mutableMapOf(),
    private val valObjects: MutableMap<String, Map<String, Value>> = mutableMapOf()
) {
    fun setVal(key: String, properties: Map<String, Value>) {
        if (valObjects[key] != null) {
            throw KoyLangRuntimeException("")
        }
        valObjects[key] = properties
    }

    fun setMutableVal(key: String, properties: Map<String, Value>) {
        if (mutableValObjects[key] != null) {
            throw KoyLangRuntimeException("")
        }
        mutableValObjects[key] = properties
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
}
