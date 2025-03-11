package service.gemini

import model.CodeSuggestion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object GeminiResponseParser {
    fun parseResponse(response: String): Pair<List<CodeSuggestion>, String?> {
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