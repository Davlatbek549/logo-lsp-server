package org.example.logo.analysis

import org.eclipse.lsp4j.Position
import org.example.logo.model.LogoDocumentAnalysis
import org.example.logo.model.LogoSymbol

object LogoAnalysisQueries {

    fun findSymbolAtPosition(
        analysis: LogoDocumentAnalysis,
        position: Position
    ): LogoSymbol? {
        return analysis.symbols
            .filter { it.contains(position) }
            .minByOrNull { symbol ->
                val start = symbol.range.start
                val end = symbol.range.end
                (end.line - start.line) * 10_000 + (end.character - start.character)
            }
    }

    fun findWordAtPosition(
        text: String,
        position: Position
    ): String? {
        val lines = text.lines()
        if (position.line !in lines.indices) return null

        val line = lines[position.line]
        if (line.isEmpty()) return null
        if (position.character < 0 || position.character >= line.length) return null

        return LogoRegex.word.findAll(line)
            .firstOrNull { position.character in it.range }
            ?.value
    }

    fun stripComment(line: String): String {
        val commentIndex = line.indexOf(';')
        return if (commentIndex >= 0) line.substring(0, commentIndex) else line
    }
}