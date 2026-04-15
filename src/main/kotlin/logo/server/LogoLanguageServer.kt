package org.example.logo.server

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import org.example.logo.util.LogoLogger
import java.util.concurrent.CompletableFuture

class LogoLanguageServer : LanguageServer {

    private var shutdownRequested = false
    private lateinit var client: LanguageClient

    private val workspaceService = LogoWorkspaceService()
    private val textDocumentService by lazy { LogoTextDocumentService(::getClient) }

    fun connect(client: LanguageClient) {
        this.client = client
    }

    fun getClient(): LanguageClient = client

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        LogoLogger.log("initialize called")
        return CompletableFuture.completedFuture(
            InitializeResult(LogoServerCapabilities.create())
        )
    }

    override fun shutdown(): CompletableFuture<Any> {
        LogoLogger.log("shutdown called")
        shutdownRequested = true
        return CompletableFuture.completedFuture(Any())
    }

    override fun exit() {
        LogoLogger.log("exit called, shutdownRequested=$shutdownRequested")
        kotlin.system.exitProcess(if (shutdownRequested) 0 else 1)
    }

    override fun getTextDocumentService(): TextDocumentService {
        LogoLogger.log("getTextDocumentService called")
        return textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService {
        LogoLogger.log("getWorkspaceService called")
        return workspaceService
    }
}