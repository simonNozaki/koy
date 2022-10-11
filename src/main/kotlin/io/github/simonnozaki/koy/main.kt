package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input

fun main() {
    val interpreter = Interpreter()

    val linesSource = """
    val lang = "koy";
    val msg = "Hello";
    println(lang + msg);
    
    val object = {
      name: "koy",
      influencedBy: ["Koy", "JavaScript", "Kotlin"]
    };
    println(object);
    
    val greet = |msg| {
      "Hello, " + msg;
    };
    greet("Koy");
    
    val Age = |value| {
      val _v = value;
      {
        v: _v,
        getOld: |_| {
          _v = _v + 1;
        }
      };
    };
    val now = Age(29);
    
    val createFunction = |msg| {
      || {
        "Hello, " + msg;
      };
    };
    val f = createFunction("Koy");
    """.trimIndent()
    Parsers.lines()
        .parse(Input.of(linesSource))
        .result
        .forEach { interpreter.interpret(it) }

    println(interpreter.getValue("now"))

    println(interpreter.getVariables())
    println(interpreter.getFunctions())

    val programInterpreter = Interpreter()
    val programSource = """
    fn greet(message) {
      "Hello, " + message;
    }
    
    fn main() {
      val r = greet("Koy");
      println(r);
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
