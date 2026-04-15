package org.example.logo.model

data class LogoDocumentAnalysis(
    val uri: String,
    val text: String,
    val symbols: List<LogoSymbol>,
    val procedures: List<ProcedureBlock>,
    val semanticTokens: List<LogoSemanticToken>
)