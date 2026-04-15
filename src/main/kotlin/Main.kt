package org.example

import org.eclipse.lsp4j.launch.LSPLauncher
import org.example.logo.server.LogoLanguageServer
import org.example.logo.util.LogoLogger
import org.eclipse.lsp4j.services.LanguageClient

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        LogoLogger.logError("Uncaught exception in thread ${thread.name}", throwable)
    }

    try {
        LogoLogger.log("Server starting")
        LogoLogger.log("Log file: ${LogoLogger.path()}")

        val server = LogoLanguageServer()

        val launcher = LSPLauncher.createServerLauncher(
            server,
            System.`in`,
            System.out
        )

        val client: LanguageClient = launcher.remoteProxy

        server.connect(client)

        LogoLogger.log("Server connected to client")
        LogoLogger.log("Listening for messages...")

        launcher.startListening().get()

        LogoLogger.log("Server stopped normally")
    } catch (t: Throwable) {
        LogoLogger.logError("Fatal error in main()", t)
        throw t
    }
}