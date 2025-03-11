package model

import kotlinx.serialization.Serializable

@Serializable
data class CodeReviewRequest(
    val fileName: String,
    val language: String = "kotlin",
    val model: String = "gemini-2.0-flash"
)