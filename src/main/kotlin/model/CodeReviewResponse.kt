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

//@Serializable
//data class Candidate(
//    val content: Content,
//    val finishReason: String,
//    val citationMetadata: CitationMetadata,
//    val avgLogprobs: Double
//)
//
//@Serializable
//data class Content(
//    val parts: List<Part>
//)
//
//@Serializable
//data class Part(
//    val text: String
//)
//
//@Serializable
//data class CitationMetadata(
//    val citationSources: List<CitationSource>
//)
//
//@Serializable
//data class CitationSource(
//    val startIndex: Int,
//    val endIndex: Int,
//    val uri: String? = null
//)
