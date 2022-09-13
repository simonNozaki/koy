package io.github.simonnozaki.koys

import org.junit.jupiter.api.Test

import io.github.simonnozaki.koys.Expression.*
import kotlin.test.assertEquals

/**
 * Interpreter specs
 */
class InterpreterTests {
    private val interpreter = Interpreter()

    @Test
    fun can_add_10_to_20() {
        val expression = add(
            IntegerLiteral(10), IntegerLiteral(20)
        )
        val result = interpreter.interpret(expression)
        assertEquals(30, result)
    }

    @Test
    fun can_walk_tree_nodes() {
        val expression = add(
            multiply(
                IntegerLiteral(3), IntegerLiteral(5)
            ),
            IntegerLiteral(10)
        )

        val result = interpreter.interpret(expression)

        assertEquals(25, result)
    }
}