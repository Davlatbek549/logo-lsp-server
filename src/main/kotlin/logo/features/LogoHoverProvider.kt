package org.example.logo.features

import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.example.logo.analysis.LogoAnalysisQueries
import org.example.logo.model.SymbolType
import org.example.logo.state.LogoDocumentStore

class LogoHoverProvider(
    private val store: LogoDocumentStore
) {

    fun getHover(params: HoverParams): Hover? {
        val uri = params.textDocument.uri
        val position = params.position
        val analysis = store.getAnalysis(uri) ?: return null

        val symbol = LogoAnalysisQueries.findSymbolAtPosition(analysis, position)
        val content = when {
            symbol != null -> buildSymbolHover(symbol.type, symbol.name)
            else -> {
                val word = LogoAnalysisQueries.findWordAtPosition(analysis.text, position) ?: return null
                getKeywordHover(word) ?: buildGenericWordHover(word)
            }
        }

        return Hover().apply {
            contents = Either.forRight(
                MarkupContent().apply {
                    kind = MarkupKind.MARKDOWN
                    value = content
                }
            )
        }
    }

    private fun buildSymbolHover(type: SymbolType, name: String): String {
        return when (type) {
            SymbolType.PROCEDURE_DECLARATION ->
                "### Procedure Declaration\n`$name`\n\nThis declares a procedure."

            SymbolType.PROCEDURE_REFERENCE ->
                "### Procedure Call\n`$name`\n\nThis calls a procedure."

            SymbolType.VARIABLE_DECLARATION ->
                "### Variable Declaration\n`$name`\n\nThis declares or defines a variable."

            SymbolType.VARIABLE_REFERENCE ->
                "### Variable Reference\n`$name`\n\nThis reads a variable value."
        }
    }

    private fun getKeywordHover(word: String): String? {
        return when (word.uppercase()) {
            "TO" -> "### Keyword: TO\nStarts a procedure declaration."
            "END" -> "### Keyword: END\nEnds a procedure declaration."
            "MAKE" -> "### Keyword: MAKE\nCreates or updates a variable."
            "LOCAL" -> "### Keyword: LOCAL\nDeclares a local variable."
            "FORWARD", "FD" -> "### Command: FORWARD\nMoves the turtle forward."
            "BACK", "BK" -> "### Command: BACK\nMoves the turtle backward."
            "LEFT", "LT" -> "### Command: LEFT\nTurns the turtle left."
            "RIGHT", "RT" -> "### Command: RIGHT\nTurns the turtle right."
            "REPEAT" -> "### Keyword: REPEAT\nRepeats commands multiple times."
            "IF" -> "### Keyword: IF\nExecutes commands conditionally."
            "IFELSE" -> "### Keyword: IFELSE\nChooses between two branches."
            "PRINT" -> "### Command: PRINT\nPrints a value."
            "PU", "PENUP" -> "### Command: PENUP\nLifts the pen so movement does not draw."
            "PD", "PENDOWN" -> "### Command: PENDOWN\nLowers the pen so movement draws."
            "HOME" -> "### Command: HOME\nMoves the turtle to the home position."
            "CS", "CLEARSCREEN" -> "### Command: CLEARSCREEN\nClears the screen and resets the turtle."
            else -> null
        }
    }

    private fun buildGenericWordHover(word: String): String {
        return "### Word\n`$word`\n\nThis is a word in the LOGO file."
    }
}