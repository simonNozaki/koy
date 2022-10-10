package io.github.simonnozaki.koy

import java.nio.file.Files
import java.nio.file.Paths


object KoyLang {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 2) {
            throw KoyLangRuntimeException("Boot koy application with 2 arguments.")
        }

        when (args[0]) {
            "-f" -> {
                val fileName = args[1]
                val path = Paths.get(fileName)
                println(path)
                if (!Files.exists(path)) {
                    throw KoyLangRuntimeException("File [ $fileName ] is not found.")
                }
            }
            else -> System.err.println("""
            |Usage: java -jar koy.jar -f <fileName>
            |<fileName>   : read a program from <fileName> and execute it
            """.trimIndent())
        }
    }
}
