package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import java.nio.file.Files
import java.nio.file.Paths

internal val interpreter = Interpreter()

fun main(args: Array<String>) {
    if (args.size != 2) {
        throw KoyLangRuntimeException("Boot koy application with 2 arguments.")
    }

    when (args[0]) {
        "-f" -> {
            val fileName = args[1]
            val content = getFileContent(fileName)

            val program = Parsers.program().parse(Input.of(content)).result
            interpreter.callMain(program)
        }
        else -> System.err.println("""
        |Usage: java -jar koy.jar -f <fileName>
        |<fileName>   : read a program from <fileName> and execute it
        """.trimIndent())
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
