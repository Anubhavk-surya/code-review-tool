package model

import kotlinx.serialization.Serializable

@Serializable
data class CodeReviewRequest(
    val fileName: String,
    val language: String = "Kotlin",
    val model: String = "gemini-2.0-flash",
    val task: String = "code review",
    val includeInlineComments: Boolean = true,
    val includeHeaderComments: Boolean = true
)