package io.github.simonnozaki.koys

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParsersTests {
    private val interpreter = Interpreter()

    @Test
    fun can_define_and_call_function() {
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
        val program = """
           i = 0;
           while (i < 10) {
             i = i + 1;
           }
        """.trimIndent()
        val statements = Parsers.lines()
            .parse(Input.of(program))
            .result
        for (statement in statements) {
            interpreter.interpret(statement)
        }

        assertEquals(3, interpreter.getValue("i"))
    }
}