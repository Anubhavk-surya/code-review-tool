package model

import kotlinx.serialization.Serializable

@Serializable
data class CodeReviewResponse(
    val fileName: String,
    val suggestions: List<CodeSuggestion>,
    val updatedCode: String
)

@Serializable
data class CodeSuggestion(
    val lineNumber: Int,
    val originalCode: String,
    val suggestion: String,
    val explanation: String
)
