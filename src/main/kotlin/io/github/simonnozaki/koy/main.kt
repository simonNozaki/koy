package io.github.simonnozaki.koy

import org.javafp.data.Unit
import org.javafp.parsecj.Parser
import org.javafp.parsecj.Text
import org.javafp.parsecj.input.Input

val SPACING: Parser<Char, Unit> = Text.wspace.map { Unit.unit }.or(Text.regex("(?m)//.*$").map { Unit.unit })
val SPACINGS: Parser<Char, Unit> = SPACING.many().map { Unit.unit }
val D_QUOTE: Parser<Char, Unit> = Text.string("\"").then(SPACINGS)

fun main() {
    val interpreter = Interpreter()

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


    val programSource = """
    fn greet(message) {
      message;
    }
    
    fn main() {
      r = "Hello, " + "Koy";
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