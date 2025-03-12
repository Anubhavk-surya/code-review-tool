package com.suryadigital.training.codereview

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import routes.codeReviewRoute
import utils.EnvUtils
import org.koin.ktor.plugin.Koin
import di.appModule

fun main() {
    val port = EnvUtils.getEnvVariable("PORT")?.toInt() ?: 8080

    embeddedServer(factory = Netty, port = port) {
        configureServer()
    }.start(wait = true)
}

internal fun Application.configureServer() {
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
