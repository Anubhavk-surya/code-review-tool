package com.suryadigital.training.codereview

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import routes.codeReviewRoute
import utils.EnvUtils
import org.koin.ktor.plugin.Koin
import di.appModule

fun main() {
    val port = EnvUtils.getEnvVariable("PORT")?.toInt() ?: 8080

    embeddedServer(Netty, port = port) {
        configureServer()
    }.start(wait = true)
}

fun Application.configureServer() {
    install(ContentNegotiation) {
        json()
    }
    
    install(Koin) {
        modules(appModule)
    }

    routing {
        codeReviewRoute()
    }
}
