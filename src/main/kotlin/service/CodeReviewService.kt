package service

import model.CodeReviewRequest
import model.CodeReviewResponse
import model.CodeSuggestion
import utils.FileUtils

import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CodeReviewService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String,
    private val defaultModel: String
) {
    suspend fun reviewCode(request: CodeReviewRequest): CodeReviewResponse {
        println("\n=== Code Review for ${request.fileName} ===")
        println("Reading file...")
        val code = FileUtils.readFile(request.fileName)
        
        val prompt = """
            REVIEW AND ADD APPROPRIATE VISIBILITY MODIFIERS.
            
            RULES:
            1. Only add visibility modifiers when they provide meaningful encapsulation
            2. Use 'private' ONLY for:
               - Properties that contain sensitive data (API keys, credentials)
               - Helper methods that are truly internal implementation details
               - Methods that should never be called from outside the class
            3. Use 'internal' ONLY for:
               - Classes that are truly module-specific
               - Components that should never be used outside the module
            4. Use 'protected' ONLY for:
               - Methods that are meant to be overridden in subclasses
               - Properties that should be accessible in subclasses
            5. Use 'public' (default) for:
               - All public API methods and properties
               - All interface methods
               - All route handlers and endpoints
               - All extension functions
               - All top-level functions and properties
               - All data classes and their properties
               - All enum classes and their members
            
            IMPORTANT:
            - Do NOT add visibility modifiers to imports
            - Do NOT add visibility modifiers to package declarations
            - Do NOT add visibility modifiers to lambda expressions
            - Do NOT add visibility modifiers to catch blocks
            - Do NOT add visibility modifiers to route handlers
            - Do NOT add visibility modifiers to properties that are part of a public API
            
            Format:
            
            SUGGESTIONS:
            ---
            Line: <number>
            Original: class X {
            Suggestion: internal class X {
            Explanation: Added internal modifier as this class should only be accessible within the module
            ---
            
            UPDATED_CODE:
            ```kotlin
            // Code with appropriate visibility modifiers
            <code with visibility modifiers>
            ```
            
            REVIEW THIS:
            ```${request.language.lowercase()}
            $code
            ```
        """.trimIndent()

        val selectedModel = request.model
        println("Analyzing code with Gemini AI ($selectedModel)...")
        val response = makeGeminiApiCall(prompt, selectedModel)
        
        val (suggestions, updatedCode) = parseGeminiResponse(response)
        
        printReviewSummary(request.fileName, suggestions)
        
        if (updatedCode != null) {
            println("\nWriting updated code to reviewed_${request.fileName}")
            val codeWithComments = appendReviewComments(updatedCode, suggestions)
            FileUtils.writeUpdatedFile(request.fileName, codeWithComments)
            println("Review complete! You can find the updated code in 'reviewed_${request.fileName}'")
        } else {
            println("\nNo code changes were suggested.")
        }
        println("=====================================")

        return CodeReviewResponse(
            fileName = request.fileName,
            suggestions = suggestions,
            updatedCode = updatedCode ?: code
        )
    }

    private fun printReviewSummary(fileName: String, suggestions: List<CodeSuggestion>) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        
        println("\nCode Review Summary")
        println("==================")
        println("File: $fileName")
        println("Review Date: $timestamp")
        println("Number of suggestions: ${suggestions.size}")
        
        if (suggestions.isEmpty()) {
            println("\nNo suggestions found - code looks good!")
            return
        }

        println("\nDetailed Suggestions:")
        println("-------------------")
        suggestions.forEachIndexed { index, suggestion ->
            println("\n${index + 1}. Line ${suggestion.lineNumber}:")
            println("   Original code:")
            println("      ${suggestion.originalCode}")
            println("   Suggested change:")
            println("      ${suggestion.suggestion}")
            println("   Explanation:")
            println("      ${suggestion.explanation}")
            println("   ----------------------------------------")
        }
    }

    private fun appendReviewComments(code: String, suggestions: List<CodeSuggestion>): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val reviewComments = StringBuilder(code)
        
        reviewComments.append("\n\n")
        reviewComments.append("/*\n")
        reviewComments.append(" * =================================\n")
        reviewComments.append(" * CODE REVIEW COMMENTS\n")
        reviewComments.append(" * =================================\n")
        reviewComments.append(" * Review Date: $timestamp\n")
        reviewComments.append(" * \n")
        reviewComments.append(" * Changes Suggested:\n")
        
        suggestions.forEachIndexed { index, suggestion ->
            reviewComments.append(" * \n")
            reviewComments.append(" * ${index + 1}. Line ${suggestion.lineNumber}:\n")
            reviewComments.append(" *    Original: ${suggestion.originalCode}\n")
            reviewComments.append(" *    Changed to: ${suggestion.suggestion}\n")
            reviewComments.append(" *    Reason: ${suggestion.explanation}\n")
        }
        
        reviewComments.append(" * \n")
        reviewComments.append(" * End of Review Comments\n")
        reviewComments.append(" * =================================\n")
        reviewComments.append(" */")
        
        return reviewComments.toString()
    }

    private suspend fun makeGeminiApiCall(prompt: String, model: String): String {
        try {
            val response = httpClient.post("$baseUrl$model:generateContent?key=$apiKey") {
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

    private fun parseGeminiResponse(response: String): Pair<List<CodeSuggestion>, String?> {
        try {
            val jsonResponse = Json.parseToJsonElement(response)
            val text = jsonResponse.jsonObject["candidates"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.content
                ?: throw IllegalStateException("Invalid response format from Gemini API")

            val suggestions = mutableListOf<CodeSuggestion>()
            var updatedCode: String? = null
            
            val sections = text.split("UPDATED_CODE:")
            
            if (sections.isNotEmpty()) {
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
                
                if (sections.size > 1) {
                    updatedCode = sections[1].trim()
                        .removePrefix("```${sections[1].trim().takeWhile { !it.isWhitespace() }}")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()
                }
            }

            return Pair(suggestions, updatedCode)
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
