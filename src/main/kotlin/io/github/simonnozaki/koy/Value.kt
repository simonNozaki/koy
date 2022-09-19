package io.github.simonnozaki.koy

/**
 * Domain object that represents type of values.
 */
sealed class Value {
    fun asInt(): Int {
        return this as Int
    }
    fun asArray(): Array {
        return this as Array
    }

    data class Int(
        val value: kotlin.Int
    ) : Value()

    data class Array(
        val items: List<Value>
    ) : Value()

    companion object {
        /**
         * Factory method of sealed class `Value`.
         */
        fun of(v: Any): Value {
            return when(v) {
                is kotlin.Int -> Int(v)
                is List<*> -> Array(v as List<Value>)
                else -> throw RuntimeException("Type of $v can not convert")
            }
        }
    }
}
