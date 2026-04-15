package org.example.logo.analysis

object LogoLanguageDefinition {

    val keywords = setOf(
        "TO", "END", "MAKE", "LOCAL",
        "FORWARD", "FD", "BACK", "BK", "LEFT", "LT", "RIGHT", "RT",
        "REPEAT", "IF", "IFELSE", "PRINT", "PU", "PD", "PENUP", "PENDOWN",
        "HOME", "CS", "CLEARSCREEN"
    )

    fun isBuiltinWord(word: String): Boolean {
        return word.uppercase() in keywords
    }
}