package org.example.logo.features

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.example.logo.analysis.LogoLanguageDefinition
import org.example.logo.analysis.LogoSymbolResolver
import org.example.logo.model.SymbolType
import org.example.logo.state.LogoDocumentStore

class LogoDiagnosticsProvider(
    private val store: LogoDocumentStore
) {

    fun getDiagnostics(uri: String): List<Diagnostic> {
        val analysis = store.getAnalysis(uri) ?: return emptyList()
        val diagnostics = mutableListOf<Diagnostic>()

        for (symbol in analysis.symbols) {
            when (symbol.type) {
                SymbolType.VARIABLE_REFERENCE -> {
                    val declaration = LogoSymbolResolver.findVariableDeclaration(analysis, symbol)
                    if (declaration == null) {
                        diagnostics += Diagnostic().apply {
                            range = symbol.range
                            severity = DiagnosticSeverity.Error
                            source = "logo-lsp"
                            message = "Undefined variable '${symbol.name}'"
                        }
                    }
                }

                SymbolType.PROCEDURE_REFERENCE -> {
                    if (!LogoLanguageDefinition.isBuiltinWord(symbol.name)) {
                        val declarations = LogoSymbolResolver.findProcedureDeclarations(
                            store.getAllAnalyses(),
                            symbol.name
                        )

                        if (declarations.isEmpty()) {
                            diagnostics += Diagnostic().apply {
                                range = symbol.range
                                severity = DiagnosticSeverity.Error
                                source = "logo-lsp"
                                message = "Undefined procedure '${symbol.name}'"
                            }
                        }
                    }
                }

                SymbolType.PROCEDURE_DECLARATION,
                SymbolType.VARIABLE_DECLARATION -> {
                    // no diagnostic
                }
            }
        }

        return diagnostics
    }
}