package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionParserSpecs {
    @Test
    fun `can define and call function`() {
        val interpreter = Interpreter()
        val source = """
            fn add(x, y) {
              x + y;
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
    fun `should print variable`() {
        val interpreter = Interpreter()
        val source = """
            val x = 10;
            
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
    fun `can call labeled function call`() {
        val source = """
            fn power(v) {
              v * v;
            }
            
            fn main() {
              power[v=5];
            }
            """
        val program = Parsers.program().parse(Input.of(source)).result
        val result = Interpreter().callMain(program)

        assertEquals(25, result.asInt().value)
    }

    @Test
    fun `should print compound greet`() {
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


    @Test
    fun `can access props in object declaration`() {
        val interpreter = Interpreter()
        val source = """
            val koy = {
              greet: |msg| {
                "Hej, " + msg;
              },
              paradigm: "object-functional",
              influencedBy: ["Kotlin", "Scala", "Clojure"]
            };
            
            fn main() {
              println(koy);
            
              val message = koy.greet("Koy!");
              println(message);
              message;
            }
            """.trimIndent()
        val program = Parsers.program().parse(Input.of(source)).result
        val result = interpreter.callMain(program)

        assertEquals("Hej, Koy!", result.asString().value)
    }
}