package model

import kotlinx.serialization.Serializable

@Serializable
data class CodeReviewRequest(
    val fileName: String,
    val language: String = "Kotlin",
    val code: String,
    val model: String = "google/gemini-flash-2.0",
    val task: String = "code review"
)