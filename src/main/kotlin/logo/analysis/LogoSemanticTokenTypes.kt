package org.example.logo.analysis

object LogoSemanticTokenTypes {
    const val KEYWORD = 0
    const val FUNCTION = 1
    const val VARIABLE = 2
    const val NUMBER = 3

    const val DECLARATION = 1 shl 0

    val LEGEND_TYPES = listOf(
        "keyword",
        "method",
        "variable",
        "number"
    )

    val LEGEND_MODIFIERS = listOf(
        "declaration"
    )
}