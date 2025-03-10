package service

import model.CodeReviewRequest
import model.CodeReviewResponse
import model.CodeSuggestion
import utils.FileUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class CodeReviewService(
    private val httpClient: HttpClient,
    private val apiKey: String
) {
    companion object {
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
    }

    suspend fun reviewCode(fileName: String): CodeReviewResponse {
        println("Reading file: $fileName")
        val code = FileUtils.readFile(fileName)
        println("File content length: ${code.length}")
        
        val prompt = """
            You are a code reviewer. Review the following code and provide feedback in this exact format:
            
            SUGGESTIONS:
            For each suggestion, use exactly this format:
            ---
            Line: <line_number>
            Original: <original_code>
            Suggestion: <suggested_code>
            Explanation: <explanation>
            ---
            
            UPDATED_CODE:
            <full_updated_code_with_all_changes>
            
            Here's the code to review:
            ```
            $code
            ```
        """.trimIndent()

        println("Making API call to Gemini")
        val response = makeGeminiApiCall(prompt)
        
        val (suggestions, updatedCode, metadata) = parseGeminiResponse(response)
        println("Review completed:")
        println("- Model version: ${metadata.modelVersion}")
        println("- Prompt tokens: ${metadata.promptTokens}")
        println("- Response tokens: ${metadata.responseTokens}")
        println("- Total tokens: ${metadata.totalTokens}")
        println("- Number of suggestions: ${suggestions.size}")
        
        // Write the updated file
        if (updatedCode != null) {
            println("Writing updated code to file")
            FileUtils.writeUpdatedFile(fileName, updatedCode)
        } else {
            println("No updated code received from Gemini")
        }

        return CodeReviewResponse(
            fileName = fileName,
            suggestions = suggestions,
            updatedCode = updatedCode ?: code // fallback to original code if no updates
        )
    }

    private suspend fun makeGeminiApiCall(prompt: String): String {
        try {
            val response = httpClient.post(GEMINI_API_URL + apiKey) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonRequest(prompt))
            }
            return response.bodyAsText()
        } catch (e: Exception) {
            println("Error calling Gemini API: ${e.message}")
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

    private data class GeminiMetadata(
        val modelVersion: String,
        val promptTokens: Int,
        val responseTokens: Int,
        val totalTokens: Int
    )

    private fun parseGeminiResponse(response: String): Triple<List<CodeSuggestion>, String?, GeminiMetadata> {
        try {
            val jsonResponse = Json.parseToJsonElement(response).jsonObject
            
            // Extract metadata
            val usageMetadata = jsonResponse["usageMetadata"]?.jsonObject
            val metadata = GeminiMetadata(
                modelVersion = jsonResponse["modelVersion"]?.jsonPrimitive?.content ?: "unknown",
                promptTokens = usageMetadata?.get("promptTokenCount")?.jsonPrimitive?.int ?: 0,
                responseTokens = usageMetadata?.get("candidatesTokenCount")?.jsonPrimitive?.int ?: 0,
                totalTokens = usageMetadata?.get("totalTokenCount")?.jsonPrimitive?.int ?: 0
            )

            // Extract main content
            val text = jsonResponse["candidates"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.content
                ?: throw IllegalStateException("Invalid response format from Gemini API")

            val suggestions = mutableListOf<CodeSuggestion>()
            var updatedCode: String? = null
            
            // Split response into suggestions and updated code sections
            val sections = text.split("UPDATED_CODE:")
            
            if (sections.isNotEmpty()) {
                // Parse suggestions
                val suggestionText = sections[0]
                val suggestionBlocks = suggestionText.split("---").filter { it.contains("Line:") }
                
                for (block in suggestionBlocks) {
                    val lines = block.trim().split("\n")
                    val suggestionMap = mutableMapOf<String, String>()
                    
                    for (line in lines) {
                        when {
                            line.startsWith("Line:") -> suggestionMap["lineNumber"] = line.substringAfter("Line:").trim()
                            line.startsWith("Original:") -> suggestionMap["originalCode"] = line.substringAfter("Original:").trim()
                            line.startsWith("Suggestion:") -> suggestionMap["suggestion"] = line.substringAfter("Suggestion:").trim()
                            line.startsWith("Explanation:") -> suggestionMap["explanation"] = line.substringAfter("Explanation:").trim()
                        }
                    }
                    
                    if (suggestionMap.size == 4) {
                        suggestions.add(createSuggestion(suggestionMap))
                    }
                }
                
                // Extract updated code
                if (sections.size > 1) {
                    updatedCode = sections[1].trim()
                        .removePrefix("```kotlin").removePrefix("```") // Remove code block markers and language tag
                        .removeSuffix("```")
                        .trim()
                }
            }
            
            return Triple(suggestions, updatedCode, metadata)
        } catch (e: Exception) {
            println("Error parsing Gemini response: ${e.message}")
            throw e
        }
    }

    private fun createSuggestion(map: Map<String, String>): CodeSuggestion {
        return CodeSuggestion(
            lineNumber = map["lineNumber"]?.toIntOrNull() ?: 0,
            originalCode = map["originalCode"] ?: "",
            suggestion = map["suggestion"] ?: "",
            explanation = map["explanation"] ?: ""
        )
    }
}
