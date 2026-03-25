package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Error handling specs for the interpreter:
 * - Type mismatch in binary operations (#2)
 * - Parameter count mismatch in function/method calls (#6)
 */
class InterpreterErrorSpecs {

    // --- #2: Type mismatch in arithmetic/comparison operations ---

    @Test
    fun `should throw on subtract with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(subtract(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on multiply with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(multiply(str("hello"), integer(2)))
        }
    }

    @Test
    fun `should throw on divide with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(divide(str("hello"), integer(2)))
        }
    }

    @Test
    fun `should throw on remainder with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(remain(str("hello"), integer(2)))
        }
    }

    @Test
    fun `should throw on less-than with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(lessThan(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on less-than-or-equal with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(lessThanEqual(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on greater-than with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(greaterThan(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on greater-than-or-equal with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(greaterThanEqual(str("hello"), integer(1)))
        }
    }

    // --- #6: Parameter count mismatch ---

    @Test
    fun `should throw on function call with too few args`() {
        val topLevels = listOf(
            defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
            defineFunction("main", listOf(), Block(call("add", integer(1))))
        )
        assertThrows<KoyLangRuntimeException> {
            Interpreter().callMain(Program(topLevels))
        }
    }

    @Test
    fun `should throw on function call with too many args`() {
        val topLevels = listOf(
            defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
            defineFunction("main", listOf(), Block(call("add", integer(1), integer(2), integer(3))))
        )
        assertThrows<KoyLangRuntimeException> {
            Interpreter().callMain(Program(topLevels))
        }
    }

    @Test
    fun `should throw on method call with too few args`() {
        val interpreter = Interpreter()
        interpreter.interpret(
            ValDeclaration(
                "obj",
                Object(
                    mapOf(
                        "add" to FunctionLiteral(
                            listOf("x", "y"),
                            Block(add(identifier("x"), identifier("y")))
                        )
                    )
                )
            )
        )
        assertThrows<KoyLangRuntimeException> {
            interpreter.interpret(
                MethodCall(Identifier("obj"), Identifier("add"), listOf(integer(1)))
            )
        }
    }

    @Test
    fun `should throw on method call with too many args`() {
        val interpreter = Interpreter()
        interpreter.interpret(
            ValDeclaration(
                "obj",
                Object(
                    mapOf(
                        "add" to FunctionLiteral(
                            listOf("x", "y"),
                            Block(add(identifier("x"), identifier("y")))
                        )
                    )
                )
            )
        )
        assertThrows<KoyLangRuntimeException> {
            interpreter.interpret(
                MethodCall(Identifier("obj"), Identifier("add"), listOf(integer(1), integer(2), integer(3)))
            )
        }
    }
}
