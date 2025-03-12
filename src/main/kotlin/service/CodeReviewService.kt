package service

import model.CodeReviewRequest
import model.CodeReviewResponse
import utils.FileUtils
import service.gemini.GeminiApiClient
import service.gemini.GeminiResponseParser
import service.report.ReviewReporter
import io.ktor.client.HttpClient
import utils.LoggerUtils
import java.io.File

internal class CodeReviewService(
    httpClient: HttpClient,
    apiKey: String,
    baseUrl: String,
) {
    private val logger = LoggerUtils.logger<CodeReviewService>()
    private val geminiClient = GeminiApiClient(httpClient, apiKey, baseUrl)

    suspend fun reviewCode(request: CodeReviewRequest): CodeReviewResponse {
        logger.info("Starting code review for file: {}", request.fileName)
        
        try {
            FileUtils.checkLanguage(request.fileName, request.language)
        } catch (e: IllegalArgumentException) {
            logger.error("Language mismatch: {}", e.message)
            throw e
        }
        
        logger.debug("Reading file contents...")
        val code = FileUtils.readFile(request.fileName, request.language)
        
        val prompt = """
            REVIEW AND APPLY VISIBILITY MODIFIERS AND CORRECT SYNTAX ERRORS

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
            
            Error Correction Rules:
            * Correct any syntax errors found in the code.
            * Ensure that all code is valid Kotlin.
            * If a print statement is encountered, replace it with a valid `println()` call.
            * If a function is defined without a return type, and it returns a value, add the return type.
            
            Format:
            SUGGESTIONS:
            Line: <number>
            Original: class X {
            Suggestion: internal class X {
            Explanation: Restricted to module scope
            
            UPDATED_CODE:
            // Code with applied visibility modifiers and corrected syntax errors
            <updated code>  
            Review this:
            ```${request.language.lowercase()}
            $code
            ```  
        """.trimIndent()

        val selectedModel = request.model
        logger.info("Analyzing code with Gemini AI model: {}", selectedModel)
        val response = geminiClient.generateContent(prompt, selectedModel)
        
        val (suggestions, updatedCode) = GeminiResponseParser.parseResponse(response)
        
        logger.info("Code review completed. Found {} suggestions", suggestions.size)
        ReviewReporter.printReviewSummary(request.fileName, suggestions)
        
        if (updatedCode != null) {
            logger.debug("Writing updated code to reviewed_{}", request.fileName)
            val codeWithComments = ReviewReporter.appendReviewComments(updatedCode, suggestions)
            FileUtils.writeUpdatedFile(request.fileName, codeWithComments)
            logger.info("Review complete! Updated code written to 'reviewed_{}'", request.fileName)
        } else {
            logger.info("No code changes were suggested")
        }

        val reviewedFileName = "reviewed_" + File(request.fileName).name
        val reviewedFilePath = File(request.fileName).parent?.let { "$it/$reviewedFileName" } ?: reviewedFileName

        return CodeReviewResponse(
            reviewedFilePath = if (updatedCode != null) reviewedFilePath else request.fileName,
            suggestions = suggestions,
            updatedCode = updatedCode ?: code
        )
    }
}
