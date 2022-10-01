package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import org.junit.jupiter.api.Test
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
            // fn main() {
            //   add(10, 20)
            // }
            defineFunction(
                "main",
                listOf(),
                Block(
                    Println(call("add", integer(10), integer(20)))
                )
            ),
            // fn add(v1, v2) {
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
            // fn factorial(v) {
            //   if (v < 2) {
            //     1
            //   }
            //   factorial(v - 1) * v
            // }
            defineFunction(
                "factorial", listOf("v"),
                Block(
                    If(
                        lessThan(identifier("v"), integer(2)),
                        integer(1),
                        multiply(
                            call("factorial", subtract(identifier("v"), integer(1))),
                            identifier("v")
                        )
                    )
                )
            ),
            // fn main() {
            //   factorial(5)
            // }
            defineFunction(
                "main", listOf(),
                Block(
                    Println(call("factorial", integer(5)))
                )
            )
        )

        val result = interpreter.callMain(Program(topLevels))

        assertEquals(120, result.asInt().value)
    }

    @Test
    fun can_increment_in_while() {
        val statements = listOf(
            // mutable val i = 0;
//            assign("i", IntegerLiteral(0)),
            MutableValDeclaration("i", IntegerLiteral(0)),
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
        // val a = [1, 3, 5]
        val statement = ValDeclaration("a", Array(IntegerLiteral(1), IntegerLiteral(3), IntegerLiteral(5)))
        interpreter.interpret(statement)

        val arrayItems = interpreter.getValue("a")?.asArray()
        assertEquals(3, arrayItems?.items?.size)
    }

    @Test
    fun can_evaluate_object() {
        val interpreter = Interpreter()
        // val o = { a: 1, b: "1" }
        val statement = ValDeclaration(
            "o",
            Object(
                mapOf(
                    "a" to integer(1),
                    "b" to str("1")
                )
            )
        )
        interpreter.interpret(statement)
        val o = interpreter.getValue("o")?.asObject()?.value

        assertEquals(1, o?.get("a")?.asInt()?.value)
        assertEquals("1", o?.get("b")?.asString()?.value)
    }

    @Test
    fun `can define function literal`() {
        val interpreter = Interpreter()
        // val f = { x, y ->
        //   x+y;
        // };
        val statement = ValDeclaration(
            "f",
            FunctionLiteral(
                listOf("x", "y"),
                BlockExpression(
                    listOf(
                        add(identifier("x"), identifier("y"))
                    )
                )
            )
        )
        interpreter.interpret(statement)
        val f = interpreter.getFunction("f")
        println(f)

        assertEquals("f", f.name)
        assertEquals(
            true,
            f.args?.containsAll(
                listOf(
                    "x",
                    "y"
                )
            )
        )
    }

    @Test
    fun `can increment and decrement`() {
        val interpreter = Interpreter()
        listOf(
            // val n = 1;
            // val n2 = n++;
            // val m = 1;
            // val m2 = m--;
            ValDeclaration("n", integer(1)),
            ValDeclaration("n2", increment("n")),
            ValDeclaration("m", integer(1)),
            ValDeclaration("m2", decrement("m"),)
        ).forEach { interpreter.interpret(it) }
        println(interpreter.getVariables())

        assertEquals(2, interpreter.getValue("n2")?.asInt()?.value)
        assertEquals(0, interpreter.getValue("m2")?.asInt()?.value)
    }
}
