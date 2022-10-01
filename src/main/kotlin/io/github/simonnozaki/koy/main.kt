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
      influencedBy: ["Koy", "JavaScript", "Kotlin"]
    };
    println(object);
    
    greet = |msg| {
      "Hello, " + msg;
    };
    greet("Koy");
    
    Age = |value| {
      _v = value;
      {
        v: _v,
        getOld: |_| {
          _v = _v + 1;
        }
      };
    };
    now = Age(29);
    
    createFunction = |msg| {
      || {
        "Hello, " + msg;
      };
    };
    f = createFunction("Koy");
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
