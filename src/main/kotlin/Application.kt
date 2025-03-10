package com.suryadigital.training.codereview

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.config.*
import routes.codeReviewRoute
import service.CodeReviewService
import utils.EnvUtils

fun main() {
    // Load environment variables
    val port = EnvUtils.getEnvVariable("PORT")?.toInt() ?: 8080
    val apiKey = EnvUtils.requireEnvVariable("GEMINI_API_KEY")
    val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
    val defaultModel = "gemini-2.0-flash"

    embeddedServer(Netty, port = port) {
        configureServer(apiKey, baseUrl, defaultModel)
    }.start(wait = true)
}

fun Application.configureServer(
    apiKey: String,
    baseUrl: String,
    defaultModel: String
) {
    // Install content negotiation
    install(ContentNegotiation) {
        json()
    }

    // Initialize services
    val httpClient = HttpClient(CIO)
    val codeReviewService = CodeReviewService(
        httpClient = httpClient,
        apiKey = apiKey,
        baseUrl = baseUrl,
        defaultModel = defaultModel
    )

    // Configure routes
    routing {
        codeReviewRoute(codeReviewService)
    }
}
