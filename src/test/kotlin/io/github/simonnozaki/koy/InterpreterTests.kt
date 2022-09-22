package io.github.simonnozaki.koy

import org.junit.jupiter.api.Test

import io.github.simonnozaki.koy.Expression.*
import io.github.simonnozaki.koy.Object
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
        val result = interpreter.interpret(expression).asInt().value
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

        val result = interpreter.interpret(expression).asInt().value

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

        assertEquals(30, result.asInt().value)
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
                    lessThan(identifier("v"), integer(2)),
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

        assertEquals(120, result.asInt().value)
    }

    @Test
    fun can_increment_in_while() {
        val statements = listOf(
            // i = 0;
            assign("i", IntegerLiteral(0)),
            // while(x < 10) {
            //   i = i + 1;
            // }
            While(
                BinaryExpression(Operator.LESS_THAN, identifier("i"), IntegerLiteral(10)),
                assign("i", BinaryExpression(Operator.ADD, identifier("i"), IntegerLiteral(1)))
            )
        )
        for (statement in statements) {
            interpreter.interpret(statement)
        }
        assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
    }

    @Test
    fun can_define_array_literal() {
        // a = [1, 3, 5]
        val statement = assign("a", Array(IntegerLiteral(1), IntegerLiteral(3), IntegerLiteral(5)))
        interpreter.interpret(statement)

        val arrayItems = interpreter.getValue("a")?.asArray()
        assertEquals(3, arrayItems?.items?.size)
    }

    @Test
    fun can_evaluate_object() {
        val interpreter = Interpreter()
        // o = { a: 1, b: "1" }
        val statement = assign("o", Object(mapOf(
            "a" to integer(1),
            "b" to str("1")
        )))
        interpreter.interpret(statement)
        val o = interpreter.getValue("o")?.asObject()?.value

        assertEquals(1, o?.get("a")?.asInt()?.value)
        assertEquals("1", o?.get("b")?.asString()?.value)
    }
}