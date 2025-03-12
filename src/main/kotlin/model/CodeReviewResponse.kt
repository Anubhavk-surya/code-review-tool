package model

import kotlinx.serialization.Serializable

@Serializable
internal data class CodeReviewResponse(
    val reviewedFilePath: String,
    val suggestions: List<CodeSuggestion>,
    val updatedCode: String
)

@Serializable
internal data class CodeSuggestion(
    val lineNumber: Int,
    val originalCode: String,
    val suggestion: String,
    val explanation: String
)
