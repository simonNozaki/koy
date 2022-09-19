package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input

fun main() {
    val interpreter = Interpreter()

    val programSource = """
    fn greet(message) {
      "Hello, " + message;
    }
    
    fn main() {
      r = greet[message="Koy"];
    }
    """
    val program = Parsers.program().parse(Input.of(programSource)).result
    val result = interpreter.callMain(program)
    println(result)

    val linesSource = """
    lang = "koy";
    msg = "Hello";
    lang + msg;
    """.trimIndent()
    Parsers.lines()
        .parse(Input.of(linesSource))
        .result
        .forEach { interpreter.interpret(it) }

    println(interpreter.getVariables())
}