package io.github.simonnozaki.koy

/**
 * Domain object that represents type of values.
 */
sealed class Value {
    fun asInt(): Int = this as Int

    fun asArray(): Array = this as Array

    fun asBool(): Bool = this as Bool

    fun asString(): String = this as String

    fun asObject(): Object = this as Object

    fun isString(): Boolean = this is String

    fun isInt(): Boolean = this is Int

    fun isBool(): Boolean = this is Bool

    data class Int(
        val value: kotlin.Int
    ) : Value()

    data class Array(
        val items: List<Value>
    ) : Value()

    data class Bool(
        val value: Boolean
    ) : Value()

    data class String(
        val value: kotlin.String
    ) : Value()

    data class Object(
        val value: Map<kotlin.String, Value>
    ) : Value()

    data class Function(
        val args: List<kotlin.String>,
        val body: Expression.BlockExpression
    ) : Value()

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
                else -> throw RuntimeException("Type of $v can not convert")
            }
        }

        fun ofObject(v: Map<kotlin.String, Value>) = Object(v)

        fun ofFunction(args: List<kotlin.String>, body: Expression.BlockExpression) = Function(args, body)
    }
}
