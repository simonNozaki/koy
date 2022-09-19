package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParsersTests {
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
            f = false;
        """.trimIndent()
        val statements = Parsers.lines().parse(Input.of(source)).result
        statements.forEach { interpreter.interpret(it) }
        val t = interpreter.getValue("t")?.asBool()?.value
        val f = interpreter.getValue("f")?.asBool()?.value

        assertEquals(true, t)
        assertEquals(false, f)
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
    fun is_true_literal() {
        val expression = Parsers.expression().parse(Input.of("true")).result
        val result = Interpreter().interpret(expression)
        assertTrue(result.asBool().value)
    }
}