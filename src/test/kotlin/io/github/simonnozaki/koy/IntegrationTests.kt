package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Nested
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

    // --- Basic programs ---

    @Nested
    inner class `when evaluating basic programs` {

        @Test
        fun `should evaluate variable declarations and arithmetic`() {
            val source = """
                val x = 10;
                val y = 20;
                fn main() {
                  x + y;
                }
            """.trimIndent()
            assertEquals(30, run(source).asInt().value)
        }

        @Test
        fun `should call user-defined function`() {
            val source = """
                fn add(x, y) {
                  x + y;
                }
                fn main() {
                  add(10, 20);
                }
            """.trimIndent()
            assertEquals(30, run(source).asInt().value)
        }

        @Test
        fun `should evaluate recursive function`() {
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
            assertEquals(120, run(source).asInt().value)
        }
    }

    // --- Loops and objects ---

    @Nested
    inner class `when evaluating programs with loops and objects` {

        @Test
        fun `should accumulate with while loop`() {
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
            assertEquals(55, run(source).asInt().value)
        }

        @Test
        fun `should call method on object`() {
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
            assertEquals("Hello, Koy", run(source).asString().value)
        }

        @Test
        fun `should iterate with for-in`() {
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
            assertEquals(15, run(source).asInt().value)
        }
    }
}
