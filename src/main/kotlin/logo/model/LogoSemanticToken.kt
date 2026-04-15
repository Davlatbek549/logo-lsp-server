package org.example.logo.model

data class LogoSemanticToken(
    val line: Int,
    val startChar: Int,
    val length: Int,
    val tokenType: Int,
    val tokenModifiers: Int
)