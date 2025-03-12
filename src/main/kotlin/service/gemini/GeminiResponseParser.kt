package service.gemini

import model.CodeSuggestion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import utils.LoggerUtils

internal object GeminiResponseParser {
    private val logger = LoggerUtils.logger<GeminiResponseParser>()

    internal fun parseResponse(response: String): Pair<List<CodeSuggestion>, String?> {
        try {
            logger.debug("Parsing Gemini API response")
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
                val suggestionsSection = suggestionText.split("SUGGESTIONS:").getOrNull(1)?.trim() ?: ""
                val suggestionBlocks = suggestionsSection.split("\nLine:").drop(1).map { "Line:$it" }
                
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
                        // First remove the opening code block with language identifier if present
                        .let { code ->
                            if (code.startsWith("```")) {
                                // Find the first newline after the opening ```
                                val firstNewline = code.indexOf('\n')
                                if (firstNewline != -1) {
                                    code.substring(firstNewline).trim()
                                } else {
                                    code
                                }
                            } else {
                                code
                            }
                        }
                        // Then remove any remaining ``` markers
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()
                }
            }

            logger.debug("Successfully parsed response. Found {} suggestions", suggestions.size)
            return Pair(suggestions, updatedCode)
        } catch (e: Exception) {
            logger.error("Error parsing Gemini response: {}", e.message, e)
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
