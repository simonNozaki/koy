package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Parsers Tests
 */
class ParsersTests {
    @Nested
    @DisplayName("Function and main program parsers tests")
    class FunctionParsersTests {
        @Test
        fun can_define_and_call_function() {
            val interpreter = Interpreter()
            val source = """
            fn add(n, m) {
              n + m;
            }
            fn main() {
              add(1, 2);
            }
            """
            val program = Parsers.program()
                .parse(Input.of(source))
                .result
            val result = interpreter.callMain(program)

            assertEquals(3, result.asInt().value)
        }

        @Test
        fun should_print_global_variable() {
            val interpreter = Interpreter()
            val source = """
            global x = 10;
            
            fn print(v) {
              println(v);
            }
            
            fn main() {
              print(x + 2);
            }
            """
            val program = Parsers.program().parse(Input.of(source)).result
            println(interpreter.getFunctions())
            println(program)

            val result = interpreter.callMain(program)

            assertEquals(12, result.asInt().value)
            assertEquals(10, interpreter.getValue("x")?.asInt()?.value)
        }

        @Test
        fun can_call_labeled_function_call() {
            val source = """
            fn power(n) {
              n * n;
            }
            
            fn main() {
              power[n=5];
            }
            """
            val program = Parsers.program().parse(Input.of(source)).result
            val result = Interpreter().callMain(program)

            assertEquals(25, result.asInt().value)
        }

        @Test
        fun should_print_compound_greet() {
            val source = """
            fn greet(msg) {
              "Hello " + msg;
            }
            
            fn main() {
              greet[msg="Koy"];
            }
            """
            val program = Parsers.program().parse(Input.of(source)).result
            val result = Interpreter().callMain(program)

            assertEquals("Hello Koy", result.asString().value)
        }
    }

    @Nested
    @DisplayName("Lines(assignment, expression line, for-in expression and so on) parsers tests")
    class LinesParsersTests {
        @Test
        fun should_add_in_while() {
            val interpreter = Interpreter()
            // Need to trim indents since `lines` does not ignore spaces
            val program = """
            i = 0;
            while(i < 10) {
              i = i + 1;
            }            
        """.trimIndent()
            val statements = Parsers.lines().parse(Input.of(program)).result
            for (statement in statements) {
                interpreter.interpret(statement)
            }
            println(statements)

            assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
        }

        @Test
        fun should_be_else_clause() {
            val interpreter = Interpreter()
            val source = """
                x = 5;
                if (x < 5) {
                  x = 1;
                } else {
                  x = 0;
                }
            """.trimIndent()
            val statements = Parsers.lines().parse(Input.of(source)).result
            for (statement in statements) {
                interpreter.interpret(statement)
            }
            assertEquals(0, interpreter.getValue("x")?.asInt()?.value)
        }

        @Test
        fun can_loop_for_in() {
            val interpreter = Interpreter()
            val source = """
                for (i in 0 to 9) {
                  println(i);
                  i = i + 1;
                }
            """.trimIndent()
            val statements = Parsers.lines()
                .parse(Input.of(source))
                .result
            statements.forEach { interpreter.interpret(it) }

            assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
        }

        @Test
        fun block_expression() {
            val source = """
                {
                  true;
                }
            """.trimIndent()
            val expr = Parsers.blockExpression().parse(Input.of(source)).result
            val result = Interpreter().interpret(expr)

            assertTrue(result.asBool().value)
        }

        @Test
        fun can_assign_array_literal() {
            val interpreter = Interpreter()
            val source = """
                odd = [1, 3, 5]; 
            """.trimIndent()
            println(source)
            val statements = Parsers.lines().parse(Input.of(source)).result
            statements.forEach { interpreter.interpret(it) }
            val arr = interpreter.getValue("odd")?.asArray()?.items

            assertEquals(3, arr?.size)
            assertEquals(true, arr?.containsAll(listOf(
                Value.of(1),
                Value.of(3),
                Value.of(5)
            )))
        }

        @Test
        fun can_parse_tf() {
            val interpreter = Interpreter()
            val source = """
            t = true;
            a = false;
            """.trimIndent()
            val statements = Parsers.lines().parse(Input.of(source)).result
            statements.forEach { interpreter.interpret(it) }
            val t = interpreter.getValue("t")?.asBool()?.value
            val a = interpreter.getValue("a")?.asBool()?.value

            assertEquals(true, t)
            assertEquals(false, a)
        }

        @Test
        fun can_print_string() {
            val source = """
            t = "text";
            """.trimIndent()
            val statements = Parsers.lines().parse(Input.of(source)).result
            println(statements)
            val interpreter = Interpreter()
            statements.forEach { interpreter.interpret(it) }

            assertEquals("text", interpreter.getValue("t")?.asString()?.value)
        }

        @Test
        fun `can assign function literal to variable`() {
            val interpreter = Interpreter()
            val source = """
            a = { x, y ->
              x * y;
            }
            """.trimIndent()
            val statements = Parsers.lines().parse(Input.of(source)).result
            statements.forEach { interpreter.interpret(it) }
        }
    }

    @Nested
    @DisplayName("Literal types parsers tests")
    class LiteralParsersTests {
        @Test
        fun is_true_literal() {
            val expression = Parsers.expression().parse(Input.of("true")).result
            val result = Interpreter().interpret(expression)
            assertTrue(result.asBool().value)
        }

        @Test
        fun is_false_literal() {
            val expression = Parsers.expression().parse(Input.of("false")).result
            val result = Interpreter().interpret(expression)
            assertFalse(result.asBool().value)
        }

        @Test
        fun should_be_string_literal() {
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
        fun `can define function literal`() {
            val interpreter = Interpreter()
            val source = """
            { x, y ->
              x * y;
            }
            """.trimIndent()
            val expression = Parsers.functionLiteral().parse(Input.of(source)).result
            val result = interpreter.interpret(expression)
            when (result) {
                is Value.Function -> {
                    kotlin.test.assertTrue(result.args.containsAll(listOf("x")))
                    assertTrue(result.lines[0] is Expression.BinaryExpression)
                }
                else -> throw RuntimeException()
            }
        }
    }
}