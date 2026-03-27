package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionParserSpecs {
    private fun run(source: String): Value =
        Interpreter().callMain(
            Parsers.program().parse(Input.of(source)).result,
        )

    // --- Positional function calls ---

    @Nested
    inner class `when calling functions with positional arguments` {
        @Test
        fun `should define and call function`() {
            val source = """
                fn add(x, y) {
                  x + y;
                }
                fn main() {
                  add(1, 2);
                }
            """
            assertEquals(3, run(source).asInt().value)
        }

        @Test
        fun `should print and return variable`() {
            val interpreter = Interpreter()
            val source = """
                val x = 10;

                fn print(v) {
                  println(v);
                }

                fn main() {
                  print(x + 2);
                }
            """
            val result = interpreter.callMain(Parsers.program().parse(Input.of(source)).result)
            assertEquals(12, result.asInt().value)
            assertEquals(10, interpreter.getValue("x")?.asInt()?.value)
        }

        @Test
        fun `should reference n-starting parameter in function body`() {
            val source = """
                fn greet(name) {
                  name;
                }

                fn main() {
                  greet("world");
                }
            """
            assertEquals("world", run(source).asString().value)
        }

        @Test
        fun `should access object properties and call method`() {
            val source =
                """
                val koy = {
                  greet: |msg| {
                    "Hej, " + msg;
                  },
                  paradigm: "object-functional",
                  influencedBy: ["Kotlin", "Scala", "Clojure"]
                };

                fn main() {
                  println(koy);

                  val message = koy.greet("Koy!");
                  println(message);
                  message;
                }
                """.trimIndent()
            assertEquals("Hej, Koy!", run(source).asString().value)
        }
    }

    // --- Labeled function calls ---

    @Nested
    inner class `when calling functions with labeled arguments` {
        @Test
        fun `should call labeled function with integer parameter`() {
            val source = """
                fn power(v) {
                  v * v;
                }

                fn main() {
                  power[v=5];
                }
            """
            assertEquals(25, run(source).asInt().value)
        }

        @Test
        fun `should call labeled function with string parameter`() {
            val source = """
                fn greet(msg) {
                  "Hello " + msg;
                }

                fn main() {
                  greet[msg="Koy"];
                }
            """
            assertEquals("Hello Koy", run(source).asString().value)
        }
    }
}
