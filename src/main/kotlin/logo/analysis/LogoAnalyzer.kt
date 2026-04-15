package org.example.logo.analysis

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.example.logo.model.LogoDocumentAnalysis
import org.example.logo.model.LogoSemanticToken
import org.example.logo.model.LogoSymbol
import org.example.logo.model.ProcedureBlock
import org.example.logo.model.SymbolType

object LogoAnalyzer {

    fun analyze(uri: String, text: String): LogoDocumentAnalysis {
        val state = AnalyzerState(uri, text)

        text.lines().forEachIndexed { lineIndex, rawLine ->
            val line = LogoAnalysisQueries.stripComment(rawLine)

            addKeywordTokens(lineIndex, line, state.semanticTokens)
            addNumberTokens(lineIndex, line, state.semanticTokens)

            if (processProcedureDeclaration(lineIndex, line, state)) return@forEachIndexed
            if (processProcedureEnd(lineIndex, line, state)) return@forEachIndexed

            processVariableDeclarations(lineIndex, line, state)
            processVariableReferences(lineIndex, line, state)
            processProcedureReferences(lineIndex, line, state)
        }

        return LogoDocumentAnalysis(
            uri = uri,
            text = text,
            symbols = state.symbols,
            procedures = state.procedures,
            semanticTokens = state.semanticTokens
        )
    }

    private fun processProcedureDeclaration(
        lineIndex: Int,
        line: String,
        state: AnalyzerState
    ): Boolean {
        val match = LogoRegex.procedureDeclaration.find(line) ?: return false

        val name = match.groupValues[1]
        val nameStart = match.range.first + match.value.indexOf(name)
        val nameEnd = nameStart + name.length

        state.symbols += LogoSymbol(
            name = name,
            type = SymbolType.PROCEDURE_DECLARATION,
            uri = state.uri,
            range = Range(
                Position(lineIndex, nameStart),
                Position(lineIndex, nameEnd)
            ),
            procedureScope = null
        )

        state.semanticTokens += LogoSemanticToken(
            line = lineIndex,
            startChar = nameStart,
            length = name.length,
            tokenType = LogoSemanticTokenTypes.FUNCTION,
            tokenModifiers = LogoSemanticTokenTypes.DECLARATION
        )

        state.currentProcedureName = name
        state.currentProcedureStartLine = lineIndex

        val rest = line.substring(nameEnd)
        LogoRegex.variableReference.findAll(rest).forEach { paramMatch ->
            val paramName = paramMatch.groupValues[1]
            val absoluteStart = nameEnd + paramMatch.range.first + 1
            val absoluteEnd = absoluteStart + paramName.length

            state.symbols += LogoSymbol(
                name = paramName,
                type = SymbolType.VARIABLE_DECLARATION,
                uri = state.uri,
                range = Range(
                    Position(lineIndex, absoluteStart),
                    Position(lineIndex, absoluteEnd)
                ),
                procedureScope = state.currentProcedureName
            )

            state.semanticTokens += LogoSemanticToken(
                line = lineIndex,
                startChar = absoluteStart,
                length = paramName.length,
                tokenType = LogoSemanticTokenTypes.VARIABLE,
                tokenModifiers = LogoSemanticTokenTypes.DECLARATION
            )
        }

        return true
    }

    private fun processProcedureEnd(
        lineIndex: Int,
        line: String,
        state: AnalyzerState
    ): Boolean {
        if (!LogoRegex.procedureEnd.matches(line)) return false
        val currentName = state.currentProcedureName ?: return false
        val currentStart = state.currentProcedureStartLine ?: return false

        state.procedures += ProcedureBlock(
            name = currentName,
            startLine = currentStart,
            endLine = lineIndex
        )

        state.currentProcedureName = null
        state.currentProcedureStartLine = null
        return true
    }

    private fun processVariableDeclarations(
        lineIndex: Int,
        line: String,
        state: AnalyzerState
    ) {
        processVariableDeclarationsForRegex(
            regex = LogoRegex.makeDeclaration,
            lineIndex = lineIndex,
            line = line,
            state = state
        )

        processVariableDeclarationsForRegex(
            regex = LogoRegex.localDeclaration,
            lineIndex = lineIndex,
            line = line,
            state = state
        )
    }

    private fun processVariableDeclarationsForRegex(
        regex: Regex,
        lineIndex: Int,
        line: String,
        state: AnalyzerState
    ) {
        regex.findAll(line).forEach { match ->
            val variableName = match.groupValues[1]
            val quoteIndex = line.indexOf("\"$variableName", match.range.first)
            val start = quoteIndex + 1
            val end = start + variableName.length

            state.symbols += LogoSymbol(
                name = variableName,
                type = SymbolType.VARIABLE_DECLARATION,
                uri = state.uri,
                range = Range(
                    Position(lineIndex, start),
                    Position(lineIndex, end)
                ),
                procedureScope = state.currentProcedureName
            )

            state.semanticTokens += LogoSemanticToken(
                line = lineIndex,
                startChar = start,
                length = variableName.length,
                tokenType = LogoSemanticTokenTypes.VARIABLE,
                tokenModifiers = LogoSemanticTokenTypes.DECLARATION
            )
        }
    }

    private fun processVariableReferences(
        lineIndex: Int,
        line: String,
        state: AnalyzerState
    ) {
        LogoRegex.variableReference.findAll(line).forEach { match ->
            val variableName = match.groupValues[1]
            val start = match.range.first + 1
            val end = start + variableName.length

            state.symbols += LogoSymbol(
                name = variableName,
                type = SymbolType.VARIABLE_REFERENCE,
                uri = state.uri,
                range = Range(
                    Position(lineIndex, start),
                    Position(lineIndex, end)
                ),
                procedureScope = state.currentProcedureName
            )

            state.semanticTokens += LogoSemanticToken(
                line = lineIndex,
                startChar = start,
                length = variableName.length,
                tokenType = LogoSemanticTokenTypes.VARIABLE,
                tokenModifiers = 0
            )
        }
    }

    private fun processProcedureReferences(
        lineIndex: Int,
        line: String,
        state: AnalyzerState
    ) {
        LogoRegex.word.findAll(line).forEach { match ->
            val word = match.groupValues[1]
            val upper = word.uppercase()

            if (upper in LogoLanguageDefinition.keywords) return@forEach

            val beforeIndex = match.range.first - 1
            if (beforeIndex >= 0 && line[beforeIndex] == ':') return@forEach
            if (beforeIndex >= 0 && line[beforeIndex] == '"') return@forEach

            state.symbols += LogoSymbol(
                name = word,
                type = SymbolType.PROCEDURE_REFERENCE,
                uri = state.uri,
                range = Range(
                    Position(lineIndex, match.range.first),
                    Position(lineIndex, match.range.last + 1)
                ),
                procedureScope = state.currentProcedureName
            )

            state.semanticTokens += LogoSemanticToken(
                line = lineIndex,
                startChar = match.range.first,
                length = word.length,
                tokenType = LogoSemanticTokenTypes.FUNCTION,
                tokenModifiers = 0
            )
        }
    }

    private fun addKeywordTokens(
        lineIndex: Int,
        line: String,
        semanticTokens: MutableList<LogoSemanticToken>
    ) {
        LogoRegex.word.findAll(line).forEach { match ->
            val word = match.groupValues[1]
            if (LogoLanguageDefinition.isBuiltinWord(word)) {
                semanticTokens += LogoSemanticToken(
                    line = lineIndex,
                    startChar = match.range.first,
                    length = word.length,
                    tokenType = LogoSemanticTokenTypes.KEYWORD,
                    tokenModifiers = 0
                )
            }
        }
    }

    private fun addNumberTokens(
        lineIndex: Int,
        line: String,
        semanticTokens: MutableList<LogoSemanticToken>
    ) {
        LogoRegex.number.findAll(line).forEach { match ->
            semanticTokens += LogoSemanticToken(
                line = lineIndex,
                startChar = match.range.first,
                length = match.value.length,
                tokenType = LogoSemanticTokenTypes.NUMBER,
                tokenModifiers = 0
            )
        }
    }

    private class AnalyzerState(
        val uri: String,
        text: String
    ) {
        val text: String = text
        val symbols = mutableListOf<LogoSymbol>()
        val procedures = mutableListOf<ProcedureBlock>()
        val semanticTokens = mutableListOf<LogoSemanticToken>()

        var currentProcedureName: String? = null
        var currentProcedureStartLine: Int? = null
    }
}