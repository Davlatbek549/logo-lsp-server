package org.example.logo.server

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService
import org.example.logo.util.LogoLogger

class LogoWorkspaceService : WorkspaceService {

    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        LogoLogger.log("workspace configuration changed")
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        LogoLogger.log("workspace watched files changed")
    }
}