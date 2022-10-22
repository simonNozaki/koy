package io.github.simonnozaki.koy

/**
 * Domain object that represents type of values.
 */
sealed class Value {
    abstract override fun toString(): kotlin.String

    fun asInt(): Int = this as Int

    fun asArray(): Array = this as Array

    fun asBool(): Bool = this as Bool

    fun asString(): String = this as String

    fun asObject(): Object = this as Object

    fun asSet(): Set = this as Set

    fun isString(): Boolean = this is String

    fun isInt(): Boolean = this is Int

    fun isBool(): Boolean = this is Bool

    fun isSet(): Boolean = this is Set

    fun isArray() = this is Array

    fun isObject(): Boolean = this is Object

    fun isNil() = this is Nil

    data class Int(
        val value: kotlin.Int
    ) : Value() {
        override fun toString(): kotlin.String = value.toString()
    }

    data class Array(
        val items: List<Value>
    ) : Value() {
        override fun toString(): kotlin.String = items.map { it.toString() }.toString()
    }

    data class Bool(
        val value: Boolean
    ) : Value() {
        override fun toString(): kotlin.String = value.toString()
    }

    data class String(
        val value: kotlin.String
    ) : Value() {
        override fun toString(): kotlin.String = value
    }

    data class Object(
        val value: Map<kotlin.String, Value>
    ) : Value() {
        override fun toString(): kotlin.String = value.toString()
    }

    data class Function(
        val args: List<kotlin.String>,
        val body: Expression.BlockExpression
    ) : Value() {
        override fun toString(): kotlin.String = "Function[params=$args]"
    }

    data class Set(
        val value: kotlin.collections.Set<Value>
    ) : Value() {
        override fun toString(): kotlin.String = value.toString()
    }

    object Nil : Value() {
        override fun toString() = ""
    }

    companion object {
        /**
         * Factory method of sealed class `Value`.
         */
        fun of(v: Any): Value {
            return when (v) {
                is kotlin.Int -> Int(v)
                is List<*> -> Array(v as List<Value>)
                is Boolean -> Bool(v)
                is kotlin.String -> String(v)
                is kotlin.collections.Set<*> -> Set(v as kotlin.collections.Set<Value>)
                else -> throw RuntimeException("Type of $v can not convert")
            }
        }

        fun ofObject(v: Map<kotlin.String, Value>) = Object(v)

        fun ofFunction(args: List<kotlin.String>, body: Expression.BlockExpression) = Function(args, body)
    }
}
