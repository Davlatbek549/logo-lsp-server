package org.example.logo.server

import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import org.example.logo.features.LogoDiagnosticsProvider
import org.example.logo.features.LogoHoverProvider
import org.example.logo.features.LogoNavigationProvider
import org.example.logo.features.LogoSemanticTokensEncoder
import org.example.logo.state.LogoDocumentStore
import org.example.logo.util.LogoLogger
import java.util.concurrent.CompletableFuture

class LogoTextDocumentService(
    private val clientProvider: () -> LanguageClient
) : TextDocumentService {

    private val store = LogoDocumentStore()
    private val hoverProvider = LogoHoverProvider(store)
    private val navigationProvider = LogoNavigationProvider(store)
    private val diagnosticsProvider = LogoDiagnosticsProvider(store)

    override fun didOpen(params: DidOpenTextDocumentParams) {
        val uri = params.textDocument.uri
        val text = params.textDocument.text

        LogoLogger.log("didOpen: $uri")
        store.open(uri, text)
        publishDiagnostics(uri)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val newText = params.contentChanges.lastOrNull()?.text ?: return

        LogoLogger.log("didChange: $uri")
        store.update(uri, newText)
        publishDiagnostics(uri)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        val uri = params.textDocument.uri

        LogoLogger.log("didClose: $uri")
        store.close(uri)

        clientProvider().publishDiagnostics(
            PublishDiagnosticsParams(uri, emptyList())
        )
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        LogoLogger.log("didSave: ${params.textDocument.uri}")
    }

    override fun definition(
        params: DefinitionParams
    ): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        LogoLogger.log(
            "definition called: uri=${params.textDocument.uri}, " +
                    "line=${params.position.line}, char=${params.position.character}"
        )

        return CompletableFuture.completedFuture(
            navigationProvider.definition(params)
        )
    }

    override fun declaration(
        params: DeclarationParams
    ): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        LogoLogger.log(
            "declaration called: uri=${params.textDocument.uri}, " +
                    "line=${params.position.line}, char=${params.position.character}"
        )

        return CompletableFuture.completedFuture(
            navigationProvider.declaration(params)
        )
    }

    override fun semanticTokensFull(
        params: SemanticTokensParams
    ): CompletableFuture<SemanticTokens> {
        val uri = params.textDocument.uri
        LogoLogger.log("semanticTokensFull called: $uri")

        val analysis = store.getAnalysis(uri)
        if (analysis == null) {
            LogoLogger.log("No analysis found for semantic tokens: $uri")
            return CompletableFuture.completedFuture(SemanticTokens(emptyList()))
        }

        val encoded = LogoSemanticTokensEncoder.encode(analysis.semanticTokens)
        LogoLogger.log("semanticTokensFull produced ${analysis.semanticTokens.size} tokens")

        return CompletableFuture.completedFuture(SemanticTokens(encoded))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?> {
        val uri = params.textDocument.uri
        val position = params.position

        LogoLogger.log("hover called: $uri at ${position.line}:${position.character}")

        return CompletableFuture.completedFuture(
            hoverProvider.getHover(params)
        )
    }

    private fun publishDiagnostics(uri: String) {
        val diagnostics = diagnosticsProvider.getDiagnostics(uri)

        LogoLogger.log("Publishing ${diagnostics.size} diagnostics for $uri")

        clientProvider().publishDiagnostics(
            PublishDiagnosticsParams(uri, diagnostics)
        )
    }
}