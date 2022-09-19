package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
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
}