package org.example.logo.server

import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.example.logo.analysis.LogoSemanticTokenTypes

object LogoServerCapabilities {

    fun create(): ServerCapabilities {
        val semanticTokensOptions = SemanticTokensWithRegistrationOptions().apply {
            legend = SemanticTokensLegend(
                LogoSemanticTokenTypes.LEGEND_TYPES,
                LogoSemanticTokenTypes.LEGEND_MODIFIERS
            )
            full = Either.forLeft(true)
        }

        return ServerCapabilities().apply {
            textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
            definitionProvider = Either.forLeft(true)
            declarationProvider = Either.forLeft(true)
            hoverProvider = Either.forLeft(true)
            semanticTokensProvider = semanticTokensOptions
        }
    }
}