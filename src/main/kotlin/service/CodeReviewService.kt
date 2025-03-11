package service

import model.CodeReviewRequest
import model.CodeReviewResponse
import utils.FileUtils
import service.gemini.GeminiApiClient
import service.gemini.GeminiResponseParser
import service.report.ReviewReporter
import io.ktor.client.HttpClient

class CodeReviewService(
    httpClient: HttpClient,
    apiKey: String,
    baseUrl: String,
    private val defaultModel: String
) {
    private val geminiClient = GeminiApiClient(httpClient, apiKey, baseUrl)

    suspend fun reviewCode(request: CodeReviewRequest): CodeReviewResponse {
        println("\n=== Code Review for ${request.fileName} ===")
        println("Reading file...")
        val code = FileUtils.readFile(request.fileName)
        
        val prompt = """
            REVIEW AND APPLY VISIBILITY MODIFIERS

            Rules:
            private → Only for:
            Sensitive data (API keys, credentials)
            Internal helper methods
            Methods not meant to be called externally
            internal → Only for:
            Classes and components meant to stay within the module
            protected → Only for:
            Methods and properties meant for subclasses
            public (default) → Use for:
            Public APIs, interface methods
            Route handlers, extension functions
            Top-level functions, data classes, enums
            Do NOT add modifiers to:
            Imports, package declarations
            Lambdas, catch blocks, route handlers
            Public API properties
            Format:
            SUGGESTIONS:
            Line: <number>
            Original: class X {
            Suggestion: internal class X {
            Explanation: Restricted to module scope
            
            UPDATED_CODE:
            // Code with applied visibility modifiers  
            <updated code>  
            Review this:
            ```${request.language.lowercase()}
            $code
            ```  
        """.trimIndent()

        val selectedModel = request.model
        println("Analyzing code with Gemini AI ($selectedModel)...")
        val response = geminiClient.generateContent(prompt, selectedModel)
        
        val (suggestions, updatedCode) = GeminiResponseParser.parseResponse(response)
        
        ReviewReporter.printReviewSummary(request.fileName, suggestions)
        
        if (updatedCode != null) {
            println("\nWriting updated code to reviewed_${request.fileName}")
            val codeWithComments = ReviewReporter.appendReviewComments(updatedCode, suggestions)
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
}
