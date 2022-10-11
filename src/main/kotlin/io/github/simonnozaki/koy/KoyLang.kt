package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import java.nio.file.Files
import java.nio.file.Paths

internal val interpreter = Interpreter()

fun main(args: Array<String>) {
    for ((i) in args.withIndex()) {
        if (args[i] == "-f" && args[i+1].matches(Regex(".+\\.koy"))) {
            val fileName = args[i+1]
            println(fileName)
            val content = getFileContent(fileName)

            val program = Parsers.program().parse(Input.of(content)).result
            interpreter.callMain(program)
            return
        } else if (args[i] == "-d") {
            interpreter.withDebug()
        } else {
            System.err.println("""
            |Usage: java -jar koy.jar -f <fileName> (-d)*
            | -d             : enable debug logg printing ast
            |-f <fileName>   : read a program from <fileName> and execute it
            """.trimIndent())
        }
    }
}

/**
 * Get file content from file path and name
 * @param fileName
 */
internal fun getFileContent(fileName: String): String {
    val path = Paths.get(fileName)
    if (!Files.exists(path)) {
        throw KoyLangRuntimeException("File [ $fileName ] is not found.")
    }

    Files.newBufferedReader(path).use {
        val stringBuilder = StringBuilder()
        stringBuilder.append(it.readLine())
        var line = it.readLine()
        while (line != null) {
            line = it.readLine()
            stringBuilder.append(line)
        }
        return stringBuilder.toString()
    }
}
