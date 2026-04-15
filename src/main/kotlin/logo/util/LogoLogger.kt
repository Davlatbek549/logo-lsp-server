package org.example.logo.util

import java.io.File
import java.time.LocalDateTime

object LogoLogger {

    private val logFile = File(
        System.getProperty("java.io.tmpdir"),
        "logo-lsp.log"
    )

    fun log(message: String) {
        val fullMessage = "[${LocalDateTime.now()}] $message"
        System.err.println(fullMessage)
        logFile.appendText("$fullMessage\n")
    }

    fun logError(message: String, throwable: Throwable? = null) {
        val fullMessage = "[${LocalDateTime.now()}] ERROR: $message"
        System.err.println(fullMessage)
        logFile.appendText("$fullMessage\n")

        if (throwable != null) {
            val stackTrace = throwable.stackTraceToString()
            System.err.println(stackTrace)
            logFile.appendText("$stackTrace\n")
        }
    }

    fun path(): String = logFile.absolutePath
}