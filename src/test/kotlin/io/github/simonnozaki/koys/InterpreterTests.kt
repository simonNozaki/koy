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

    @Test
    fun can_evaluate_main_function() {
        val topLevels = listOf(
            // define main() {
            //   add(10, 20)
            // }
            defineFunction(
                "main",
                listOf(),
                Block(
                    Println(call("add", integer(10), integer(20)))
                )
            ),
            // define add(v1, v2) {
            //   v1 + v2
            // }
            defineFunction(
                "add",
                listOf("v1", "v2"),
                Block(
                    add(identifier("v1"), identifier("v2"))
                )
            )
        )

        val result = interpreter.callMain(Program(topLevels))

        assertEquals(30, result)
    }

    @Test
    fun can_evaluate_factorial() {
        val topLevels = listOf(
            // define factorial(v) {
            //   if (v < 2) {
            //     1
            //   }
            //   factorial(v - 1) * v
            // }
            defineFunction("factorial", listOf("v"), Block(
                If(
                    lessThan(identifier("n"), integer(2)),
                    integer(1),
                    multiply(
                        call("factorial", subtract(identifier("v"), integer(1))),
                        identifier("v")
                    )
                )
            )),
            // define main() {
            //   factorial(5)
            // }
            defineFunction("main", listOf(), Block(
                Println(call("factorial", integer(5)))
            ))
        )

        val result = interpreter.callMain(Program(topLevels))

        assertEquals(120, result)
    }
}