package service

import model.CodeReviewRequest
import model.CodeReviewResponse
import model.CodeSuggestion
import utils.FileUtils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CodeReviewService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String,
    private val defaultModel: String,
) {
    suspend fun reviewCode(request: CodeReviewRequest): CodeReviewResponse {
        println("\n=== Code Review for ${request.fileName} ===")
        println("Reading file...")
        val code = FileUtils.readFile(request.fileName)

        val prompt = """
            You are an experienced Kotlin developer conducting a thorough code review. Review the following ${request.language} code and provide comprehensive feedback.
            
            IMPORTANT: Every declaration (classes, properties, functions) MUST have an explicit visibility modifier (private, protected, internal, or public).
            Default visibility is not acceptable - you must explicitly specify the most restrictive appropriate visibility modifier.
            
            Focus on these key areas:
            
            1. Visibility and Access Control (HIGHEST PRIORITY):
               - EVERY declaration MUST have an explicit visibility modifier
               - Use private for anything only used within its containing class/file
               - Use protected for members that should only be accessible in subclasses
               - Use internal for declarations that should be visible only within the same module
               - Use public only when the declaration needs to be visible everywhere
               - No default visibility allowed - all declarations must be explicit
            
            2. Code Structure and Design:
               - Consider if class is the right choice (would object be better for utility functions?)
               - Function design (pure functions vs side effects)
               - Unnecessary state (prefer stateless design)
               - Parameter and return types
               - Single Responsibility Principle
               - Unnecessary code or redundancy
            
            3. Kotlin Best Practices:
               - Immutability (using val over var)
               - String templates instead of concatenation
               - Single-expression functions where possible
               - Meaningful parameter names (firstNumber, secondNumber instead of a, b)
               - Proper companion object usage
               - Use of const for compile-time constants
               - Extension functions if beneficial
            
            4. API Design:
               - Intuitive and consistent method names
               - Clear parameter names
               - Proper documentation
               - Consistent return types
               - Error handling strategy
               - Interface segregation if needed
            
            5. Performance Considerations:
               - Unnecessary object creation
               - Proper scoping
               - Efficient calculations
               - Memory usage
            
            SUGGESTIONS:
            For each suggestion, use exactly this format:
            ---
            Line: <line_number>
            Original: <original_code>
            Suggestion: <suggested_code>
            Explanation: <detailed explanation including:
                        - The specific Kotlin best practice being applied
                        - Why this change improves the code
                        - Why this visibility modifier was chosen
                        - Any additional considerations or alternatives>
            ---
            
            UPDATED_CODE:
            Provide the complete updated code incorporating all suggestions. The code MUST:
            - Have explicit visibility modifiers on EVERY declaration (no default visibility allowed)
            - Be idiomatic Kotlin
            - Follow functional programming principles where appropriate
            - Be well-structured and maintainable
            - Follow all best practices
            - Be ready for production use
            
            Here's the code to review:
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
                            line.startsWith("Line:") -> suggestionMap["lineNumber"] =
                                line.substringAfter("Line:").trim()

                            line.startsWith("Original:") -> suggestionMap["originalCode"] =
                                line.substringAfter("Original:").trim()

                            line.startsWith("Suggestion:") -> suggestionMap["suggestion"] =
                                line.substringAfter("Suggestion:").trim()

                            line.startsWith("Explanation:") -> suggestionMap["explanation"] =
                                line.substringAfter("Explanation:").trim()
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
