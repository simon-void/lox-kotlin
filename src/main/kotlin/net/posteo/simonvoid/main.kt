package net.posteo.simonvoid

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: jlox [script]");
        exitProcess(64);
    } else if (args.size == 1) {
        Lox.runFile(args[0]);
    } else {
        Lox.runPrompt();
    }
}

private object Lox {
    var hadError = false

    fun runFile(path: String) {
//        val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
//        run(String(bytes, Charset.defaultCharset()))
//        if(hadError) {
//            exitProcess(65);
//        }
    }

    fun runPrompt() {
//        BufferedReader(InputStreamReader(System.`in`)).useLines { lines->
//            print("> ")
//            lines.forEach { line->
//                run(line)
//                hadError = false
//                print("> ")
//            }
//        }
    }

    private fun run(source: String) {
//        val scanner = Scanner(source, this::error)
//        val tokens: List<Token> = scanner.scanTokens()
//
//        // For now, just print the tokens.
//        for (token in tokens) {
//            println(token)
//        }
    }

    fun error(line: Int, message: String) {
        fun report(
            line: Int,
            where: String,
            message: String,
        ) {
            System.err.println("[line $line] Error$where: $message")
            hadError = true
        }

        report(line, "", message)
    }
}

typealias ErrorReporter = (line: Int, message: String)->Unit