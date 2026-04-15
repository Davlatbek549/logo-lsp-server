package org.example.logo.features

import org.example.logo.model.LogoSemanticToken

object LogoSemanticTokensEncoder {

    fun encode(tokens: List<LogoSemanticToken>): List<Int> {
        if (tokens.isEmpty()) return emptyList()

        val sorted = tokens.sortedWith(
            compareBy<LogoSemanticToken> { it.line }
                .thenBy { it.startChar }
        )

        val data = mutableListOf<Int>()
        var previousLine = 0
        var previousStart = 0

        for ((index, token) in sorted.withIndex()) {
            val deltaLine = if (index == 0) token.line else token.line - previousLine
            val deltaStart = if (index == 0 || deltaLine != 0) {
                token.startChar
            } else {
                token.startChar - previousStart
            }

            data += deltaLine
            data += deltaStart
            data += token.length
            data += token.tokenType
            data += token.tokenModifiers

            previousLine = token.line
            previousStart = token.startChar
        }

        return data
    }
}