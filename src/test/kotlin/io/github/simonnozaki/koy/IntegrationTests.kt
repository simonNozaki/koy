package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests: source string -> parser -> interpreter
 *
 * These tests verify that the parser and interpreter work correctly together.
 * Error cases and edge cases are covered by unit tests.
 */
class IntegrationTests {

    private fun run(source: String): Value {
        val program = Parsers.program().parse(Input.of(source)).result
        return Interpreter().callMain(program)
    }

    /**
     * Variable declarations and arithmetic are correctly evaluated via the parser.
     */
    @Test
    fun `can evaluate variable declarations and arithmetic`() {
        val source = """
            val x = 10;
            val y = 20;
            fn main() {
              x + y;
            }
        """.trimIndent()

        val result = run(source)
        assertEquals(30, result.asInt().value)
    }

    /**
     * Function definition and invocation are correctly evaluated via the parser.
     */
    @Test
    fun `can call user-defined function`() {
        val source = """
            fn add(x, y) {
              x + y;
            }
            fn main() {
              add(10, 20);
            }
        """.trimIndent()

        val result = run(source)
        assertEquals(30, result.asInt().value)
    }

    /**
     * Recursive function (factorial) is correctly evaluated via the parser.
     */
    @Test
    fun `can evaluate recursive function`() {
        val source = """
            fn factorial(v) {
              if (v < 2) {
                1;
              } else {
                factorial(v - 1) * v;
              }
            }
            fn main() {
              factorial(5);
            }
        """.trimIndent()

        val result = run(source)
        assertEquals(120, result.asInt().value)
    }

    /**
     * Aggregation using while loop and mutable variables is correctly evaluated via the parser.
     */
    @Test
    fun `can accumulate with while loop`() {
        val source = """
            fn main() {
              mutable val i = 1;
              mutable val acc = 0;
              while (i < 11) {
                acc = acc + i;
                i = i + 1;
              }
              acc;
            }
        """.trimIndent()

        // 1 + 2 + ... + 10 = 55
        val result = run(source)
        assertEquals(55, result.asInt().value)
    }

    /**
     * Object literals and method calls are correctly evaluated via the parser.
     */
    @Test
    fun `can call method on object`() {
        val source = """
            fn main() {
              val greeter = {
                greet: |msg| {
                  "Hello, " + msg;
                }
              };
              greeter.greet("Koy");
            }
        """.trimIndent()

        val result = run(source)
        assertEquals("Hello, Koy", result.asString().value)
    }

    /**
     * Iteration using for-in is correctly evaluated via the parser.
     */
    @Test
    fun `can iterate with for-in`() {
        val source = """
            fn main() {
              mutable val acc = 0;
              for (i in 1 to 6) {
                acc = acc + i;
              }
              acc;
            }
        """.trimIndent()

        // 1 + 2 + 3 + 4 + 5 = 15
        val result = run(source)
        assertEquals(15, result.asInt().value)
    }
}
