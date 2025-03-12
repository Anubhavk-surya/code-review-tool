package service.report

import model.CodeSuggestion
import utils.LoggerUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object ReviewReporter {
    private val logger = LoggerUtils.logger<ReviewReporter>()

    internal fun printReviewSummary(fileName: String, suggestions: List<CodeSuggestion>) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        
        logger.info("Code Review Summary for file: {}", fileName)
        logger.info("Review Date: {}", timestamp)
        logger.info("Found {} suggestions", suggestions.size)
        
        if (suggestions.isEmpty()) {
            logger.info("No suggestions found - code looks good!")
            return
        }

        logger.info("Detailed Suggestions:")
        suggestions.forEachIndexed { index, suggestion ->
            logger.info("Suggestion {} (Line {}):", index + 1, suggestion.lineNumber)
            logger.info("Original code: {}", suggestion.originalCode)
            logger.info("Suggested change: {}", suggestion.suggestion)
            logger.info("Explanation: {}", suggestion.explanation)
        }
    }

    internal fun appendReviewComments(code: String, suggestions: List<CodeSuggestion>): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val reviewComments = StringBuilder(code)
        
        reviewComments.append("\n\n")
        reviewComments.append("/*\n")
        reviewComments.append(" * =================================\n")
        reviewComments.append(" * CODE REVIEW COMMENTS\n")
        reviewComments.append(" * =================================\n")
        reviewComments.append(" * Review Date: $timestamp\n")
        reviewComments.append(" * \n")
        reviewComments.append(" * Changes Suggested:\n")
        
        suggestions.forEachIndexed { index, suggestion ->
            reviewComments.append(" * \n")
            reviewComments.append(" * ${index + 1}. Line ${suggestion.lineNumber}:\n")
            reviewComments.append(" *    Original: ${suggestion.originalCode}\n")
            reviewComments.append(" *    Changed to: ${suggestion.suggestion}\n")
            reviewComments.append(" *    Reason: ${suggestion.explanation}\n")
        }
        
        reviewComments.append(" * \n")
        reviewComments.append(" * End of Review Comments\n")
        reviewComments.append(" * =================================\n")
        reviewComments.append(" */")
        
        return reviewComments.toString()
    }
}
