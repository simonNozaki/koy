package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LiteralParserSpecs {
    @Test
    fun `should remain 0`() {
        val expression = Parsers.expression().parse(Input.of("10 % 5")).result
        val result = Interpreter().interpret(expression)
        assertEquals(0, result.asInt().value)
    }

    @Test
    fun `is true literal`() {
        val expression = Parsers.expression().parse(Input.of("true")).result
        val result = Interpreter().interpret(expression)
        Assertions.assertTrue(result.asBool().value)
    }

    @Test
    fun `is false literal`() {
        val expression = Parsers.expression().parse(Input.of("false")).result
        val result = Interpreter().interpret(expression)
        assertFalse(result.asBool().value)
    }

    @Test
    fun `should be string literal`() {
        val expression = Parsers.expression().parse(Input.of("\"text\"")).result
        val result = Interpreter().interpret(expression)
        assertEquals("text", result.asString().value)
    }

    @Test
    fun `can define object literal`() {
        val expression = Parsers.expression()
            .parse(Input.of("{a : 1, b: \"1\"}"))
            .result
        val result = Interpreter().interpret(expression)
        assertEquals(1, result.asObject().value["a"]?.asInt()?.value)
        assertEquals("1", result.asObject().value["b"]?.asString()?.value)
    }

    @Test
    fun `can define function literal with no parameter`() {
        val interpreter = Interpreter()
        val source = """
            |x| {
              "Hello, " + x; 
            }
            """.trimIndent()
        val expression = Parsers.functionLiteral().parse(Input.of(source)).result
        val result = interpreter.interpret(expression)
        when (result) {
            is Value.Function -> {
                kotlin.test.assertTrue(result.args.containsAll(listOf("x")))
                println(result)
            }
            else -> throw RuntimeException()
        }
    }

    @Test
    fun `can define set literal`() {
        val interpreter = Interpreter()
        val source = "%{\"kotlin\", \"koy\"}"
        val expression = Parsers.setLiteral().parse(Input.of(source)).result
        val result = interpreter.interpret(expression)
        when (result) {
            is Value.Set -> {
                Assertions.assertTrue(result.value.containsAll(listOf(Value.String("kotlin"), Value.String("koy"))))
            }
            else -> throw RuntimeException()
        }
    }

    @Test
    fun `should be false with true and false`() {
        val interpreter = Interpreter()
        val source = "true and false"
        val expression = Parsers.expression().parse(Input.of(source)).result
        val result = interpreter.interpret(expression)

        assertFalse(result.asBool().value)
    }

    @Test
    fun `should be true with true or false`() {
        val interpreter = Interpreter()
        val source = "true or false"
        val expression = Parsers.expression().parse(Input.of(source)).result
        val result = interpreter.interpret(expression)

        Assertions.assertTrue(result.asBool().value)
    }

    @Test
    fun `can access property in object literal`() {
        val interpreter = Interpreter()
        val source = "{ lang: \"Koy\" }.lang"
        val expression = Parsers.expression().parse(Input.of(source)).result
        val result = interpreter.interpret(expression)

        assertEquals("Koy", result.asString().value)
    }

    @Test
    fun `should be defined an array of 3 elements`() {
        val source = "[\"kotlin\", \"koy\", \"java\"]"
        val result = getValue(source)

        Assertions.assertTrue(result.isArray())
        assertEquals(3, result.asArray().size)
        Assertions.assertTrue(
            result.asArray().items.containsAll(
                setOf(
                    Value.of("kotlin"),
                    Value.of("koy"),
                    Value.of("java")
                )
            )
        )
    }

    @Test
    fun `can get element in array literal`() {
        val source = "[\"kotlin\", \"clojure\", \"koy\"]->2"
        val result = getValue(source)

        assertEquals("koy", result.asString().value)
    }

    @Test
    fun `should be nil`() {
        val source = "nil"
        val result = getValue(source)

        Assertions.assertTrue(result.isNil())
    }

    private fun getValue(source: String): Value {
        val interpreter = Interpreter()
        val expression = Parsers.expression().parse(Input.of(source)).result
        return interpreter.interpret(expression)
    }
}
