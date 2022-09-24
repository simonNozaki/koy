package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input

fun main() {
    val interpreter = Interpreter()
    Parsers.lines()
        .parse(
            Input.of(
                """
                l = x, y -> {
                  x, y;
                };
                println(l);
                """.trimIndent()
            )
        )
        .result
        .forEach { interpreter.interpret(it) }
    println(interpreter.getVariables())

    val linesSource = """
    lang = "koy";
    msg = "Hello";
    println(lang + msg);
    
    object = {
      name: "koy",
      influencedBy: ["Toys", "JavaScript", "Kotlin"]
    };
    println(object);
    
    x -> {
      "Hello, Lambda";
    };
    """.trimIndent()
    Parsers.lines()
        .parse(Input.of(linesSource))
        .result
        .forEach { interpreter.interpret(it) }

    println(interpreter.getVariables())
    println(interpreter.getFunctions())

    val programInterpreter = Interpreter()
    val programSource = """
    fn greet(message) {
      "Hello, " + message;
    }
    
    fn main() {
      r = greet("Koy");
    }
    """.trimIndent()
    val program = Parsers.program().parse(Input.of(programSource))
    if (program.isError) {
        println(program.msg.expected())
    }

    val result = programInterpreter.callMain(program.result)
    println(result)
    println(programInterpreter.getVariables())
    println(programInterpreter.getFunctions())
}
