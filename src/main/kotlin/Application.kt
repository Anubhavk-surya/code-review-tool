package com.suryadigital.training.codereview

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import routes.codeReviewRoute
import service.CodeReviewService
import utils.ConfigUtils

fun main() {
    val apiKey = ConfigUtils.requireEnvVariable("GEMINI_API_KEY")

    embeddedServer(Netty, port = 8080) {
        configureServer(apiKey)
    }.start(wait = true)
}

fun Application.configureServer(apiKey: String) {
    install(ContentNegotiation) {
        json()
    }

    val httpClient = HttpClient(CIO)
    val codeReviewService = CodeReviewService(httpClient, apiKey)

    routing {
        codeReviewRoute(codeReviewService)
    }
}
