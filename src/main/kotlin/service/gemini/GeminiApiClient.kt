package service.gemini

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.http.ContentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.put
import utils.LoggerUtils

internal class GeminiApiClient(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String
) {
    private val logger = LoggerUtils.logger<GeminiApiClient>()

    internal suspend fun generateContent(prompt: String, model: String): String {
        try {
            logger.debug("Making request to Gemini API with model: {}", model)
            val response = httpClient.post(urlString = "$baseUrl$model:generateContent?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonRequest(prompt))
            }

            logger.debug("Successfully received response from Gemini API")
            return response.bodyAsText()
        } catch (e: Exception) {
            logger.error("Error calling Gemini API: {}", e.message, e)
            throw e
        }
    }

    private fun buildJsonRequest(prompt: String): String {
        val request = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", prompt)
                        }
                    }
                }
            }
        }

        return request.toString()
    }
}
