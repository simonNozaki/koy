package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LiteralParserSpecs {

    private fun getValue(source: String): Value {
        val interpreter = Interpreter()
        val expression = Parsers.expression().parse(Input.of(source)).result
        return interpreter.interpret(expression)
    }

    // --- Primitive literals ---

    @Nested
    inner class `when parsing primitive literals` {

        @Test
        fun `should return 0 for remainder of 10 and 5`() {
            assertEquals(0, getValue("10 % 5").asInt().value)
        }

        @Test
        fun `should return true for true literal`() {
            assertTrue(getValue("true").asBool().value)
        }

        @Test
        fun `should return false for false literal`() {
            assertFalse(getValue("false").asBool().value)
        }

        @Test
        fun `should return string for string literal`() {
            assertEquals("text", getValue("\"text\"").asString().value)
        }

        @Test
        fun `should return nil for nil literal`() {
            assertTrue(getValue("nil").isNil())
        }
    }

    // --- Collection literals ---

    @Nested
    inner class `when parsing collection literals` {

        @Test
        fun `should define object literal with correct properties`() {
            val result = getValue("{a : 1, b: \"1\"}")
            assertEquals(1, result.asObject().value["a"]?.asInt()?.value)
            assertEquals("1", result.asObject().value["b"]?.asString()?.value)
        }

        @Test
        fun `should define function literal with parameter`() {
            val source = """
                |x| {
                  "Hello, " + x;
                }
            """.trimIndent()
            val expression = Parsers.functionLiteral().parse(Input.of(source)).result
            val result = Interpreter().interpret(expression)
            when (result) {
                is Value.Function -> kotlin.test.assertTrue(result.args.containsAll(listOf("x")))
                else -> throw RuntimeException()
            }
        }

        @Test
        fun `should define set literal`() {
            val expression = Parsers.setLiteral().parse(Input.of("%{\"kotlin\", \"koy\"}")).result
            val result = Interpreter().interpret(expression)
            when (result) {
                is Value.Set -> assertTrue(result.value.containsAll(listOf(Value.String("kotlin"), Value.String("koy"))))
                else -> throw RuntimeException()
            }
        }

        @Test
        fun `should define array literal with 3 elements`() {
            val result = getValue("[\"kotlin\", \"koy\", \"java\"]")
            assertTrue(result.isArray())
            assertEquals(3, result.asArray().size)
            assertTrue(
                result.asArray().items.containsAll(
                    setOf(Value.of("kotlin"), Value.of("koy"), Value.of("java"))
                )
            )
        }
    }

    // --- Expressions ---

    @Nested
    inner class `when parsing expressions` {

        @Test
        fun `should return false for logical-and of true and false`() {
            assertFalse(getValue("true and false").asBool().value)
        }

        @Test
        fun `should return true for logical-or of true and false`() {
            assertTrue(getValue("true or false").asBool().value)
        }

        @Test
        fun `should access property in object literal`() {
            assertEquals("Koy", getValue("{ lang: \"Koy\" }.lang").asString().value)
        }

        @Test
        fun `should get element in array literal by index`() {
            assertEquals("koy", getValue("[\"kotlin\", \"clojure\", \"koy\"]->2").asString().value)
        }

        @Test
        fun `should conjoin element with array literal`() {
            val result = getValue("[1, 3, 5]<-7")
            assertTrue(result.isArray())
            assertTrue(result.asArray().items.containsAll(listOf(Value.of(1), Value.of(3), Value.of(5), Value.of(7))))
        }

        @Test
        fun `should conjoin element with set literal`() {
            val result = getValue("%{1, 3, 5}<-7")
            assertTrue(result.isSet())
            assertTrue(result.asSet().value.containsAll(listOf(Value.of(1), Value.of(3), Value.of(5), Value.of(7))))
        }
    }
}
