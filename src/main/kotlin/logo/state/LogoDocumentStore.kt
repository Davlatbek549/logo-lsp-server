package org.example.logo.state

import org.example.logo.analysis.LogoAnalyzer
import org.example.logo.model.LogoDocumentAnalysis
import java.util.concurrent.ConcurrentHashMap

class LogoDocumentStore {

    private val documents = ConcurrentHashMap<String, String>()
    private val analyses = ConcurrentHashMap<String, LogoDocumentAnalysis>()

    fun open(uri: String, text: String) {
        documents[uri] = text
        analyses[uri] = LogoAnalyzer.analyze(uri, text)
    }

    fun update(uri: String, text: String) {
        documents[uri] = text
        analyses[uri] = LogoAnalyzer.analyze(uri, text)
    }

    fun close(uri: String) {
        documents.remove(uri)
        analyses.remove(uri)
    }

    fun getText(uri: String): String? = documents[uri]

    fun getAnalysis(uri: String): LogoDocumentAnalysis? = analyses[uri]

    fun getAllAnalyses(): Collection<LogoDocumentAnalysis> = analyses.values
}