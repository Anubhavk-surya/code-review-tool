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

        CodeReviewService(
            httpClient = get(),
            apiKey = apiKey,
            baseUrl = baseUrl,
        )
    }
} 
