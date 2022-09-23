package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input

fun main() {
    val interpreter = Interpreter()

    val linesSource = """
    lang = "koy";
    msg = "Hello";
    println(lang + msg);
    
    object = {
      name: "koy",
      influencedBy: ["Toys", "JavaScript", "Kotlin"]
    };
    println(object);
    """.trimIndent()
    Parsers.lines()
        .parse(Input.of(linesSource))
        .result
        .forEach { interpreter.interpret(it) }

    println(interpreter.getVariables())


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

    val result = interpreter.callMain(program.result)
    println(result)
    println(interpreter.getVariables())
}