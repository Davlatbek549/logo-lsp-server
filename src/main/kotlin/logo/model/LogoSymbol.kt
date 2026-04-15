package org.example.logo.model

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

data class LogoSymbol(
    val name: String,
    val type: SymbolType,
    val uri: String,
    val range: Range,
    val procedureScope: String? = null
) {
    fun contains(position: Position): Boolean {
        val start = range.start
        val end = range.end

        if (position.line < start.line || position.line > end.line) return false
        if (position.line == start.line && position.character < start.character) return false
        if (position.line == end.line && position.character >= end.character) return false

        return true
    }
}