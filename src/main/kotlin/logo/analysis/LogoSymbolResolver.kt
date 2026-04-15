package org.example.logo.analysis

import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.example.logo.model.LogoDocumentAnalysis
import org.example.logo.model.LogoSymbol
import org.example.logo.model.SymbolType

object LogoSymbolResolver {

    fun findProcedureDeclarations(
        analyses: Collection<LogoDocumentAnalysis>,
        name: String
    ): List<Location> {
        return analyses
            .asSequence()
            .flatMap { it.symbols.asSequence() }
            .filter {
                it.type == SymbolType.PROCEDURE_DECLARATION &&
                        it.name.equals(name, ignoreCase = true)
            }
            .map { Location(it.uri, it.range) }
            .toList()
    }

    fun findVariableDeclaration(
        analysis: LogoDocumentAnalysis,
        reference: LogoSymbol
    ): Location? {
        val candidates = analysis.symbols
            .filter {
                it.type == SymbolType.VARIABLE_DECLARATION &&
                        it.name.equals(reference.name, ignoreCase = true)
            }
            .filter { candidate ->
                when (reference.procedureScope) {
                    null -> {
                        candidate.procedureScope == null &&
                                startsBeforeOrAt(candidate.range.start, reference.range.start)
                    }

                    else -> {
                        candidate.procedureScope.equals(reference.procedureScope, ignoreCase = true) &&
                                startsBeforeOrAt(candidate.range.start, reference.range.start)
                    }
                }
            }
            .sortedWith(compareBy<LogoSymbol> { it.range.start.line }
                .thenBy { it.range.start.character })

        val bestMatch = candidates.lastOrNull()
        return bestMatch?.let { Location(it.uri, it.range) }
    }

    private fun startsBeforeOrAt(a: Position, b: Position): Boolean {
        return when {
            a.line < b.line -> true
            a.line > b.line -> false
            else -> a.character <= b.character
        }
    }
}