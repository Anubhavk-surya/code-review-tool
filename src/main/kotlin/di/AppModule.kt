package di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.dsl.module
import service.CodeReviewService
import utils.EnvUtils

internal val appModule = module {
    single { HttpClient(CIO) }

    single {
        val apiKey = EnvUtils.requireEnvVariable("GEMINI_API_KEY")
        val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
        val defaultModel = "gemini-2.0-flash"

        CodeReviewService(
            httpClient = get(),
            apiKey = apiKey,
            baseUrl = baseUrl,
            defaultModel = defaultModel
        )
    }
} 