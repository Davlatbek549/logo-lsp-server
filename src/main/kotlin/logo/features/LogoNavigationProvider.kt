package org.example.logo.features

import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.TextDocumentPositionParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.example.logo.analysis.LogoAnalysisQueries
import org.example.logo.analysis.LogoSymbolResolver
import org.example.logo.model.SymbolType
import org.example.logo.state.LogoDocumentStore

class LogoNavigationProvider(
    private val store: LogoDocumentStore
) {

    fun definition(
        params: DefinitionParams
    ): Either<MutableList<out Location>, MutableList<out LocationLink>> {
        return resolve(params)
    }

    fun declaration(
        params: DeclarationParams
    ): Either<MutableList<out Location>, MutableList<out LocationLink>> {
        return resolve(params)
    }

    private fun resolve(
        params: TextDocumentPositionParams
    ): Either<MutableList<out Location>, MutableList<out LocationLink>> {
        val result = mutableListOf<Location>()
        val uri = params.textDocument.uri
        val analysis = store.getAnalysis(uri) ?: return Either.forLeft(result)

        val symbol = LogoAnalysisQueries.findSymbolAtPosition(analysis, params.position)
            ?: return Either.forLeft(result)

        when (symbol.type) {
            SymbolType.PROCEDURE_REFERENCE -> {
                result += LogoSymbolResolver.findProcedureDeclarations(
                    store.getAllAnalyses(),
                    symbol.name
                )
            }

            SymbolType.VARIABLE_REFERENCE -> {
                val declaration = LogoSymbolResolver.findVariableDeclaration(analysis, symbol)
                if (declaration != null) {
                    result += declaration
                }
            }

            SymbolType.PROCEDURE_DECLARATION,
            SymbolType.VARIABLE_DECLARATION -> {
                // already on declaration
            }
        }

        return Either.forLeft(result)
    }
}