package io.github.simonnozaki.koys

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParsersTests {
    @Test
    fun can_define_and_call_function() {
        val interpreter = Interpreter()
        val source = """
            define add(n, m) {
              n + m;
            }
            define main() {
              add(1, 2);
            }
        """
        val program = Parsers.program()
            .parse(Input.of(source))
            .result
        val result = interpreter.callMain(program)

        assertEquals(3, result)
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

        assertEquals(10, interpreter.getValue("i"))
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
        assertEquals(0, interpreter.getValue("x"))
    }

    @Test
    fun should_print_global_variable() {
        val interpreter = Interpreter()
        val source = """
            global x = 10;
            
            define print(v) {
              println(v);
            }
            
            define main() {
              print(x + 2);
            }
        """
        val program = Parsers.program().parse(Input.of(source)).result
        println(program)

        val result = interpreter.callMain(program)

        assertEquals(12, result)
        assertEquals(10, interpreter.getValue("x"))
    }
}