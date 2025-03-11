package service.report

import model.CodeSuggestion
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ReviewReporter {
    fun printReviewSummary(fileName: String, suggestions: List<CodeSuggestion>) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        
        println("\nCode Review Summary")
        println("==================")
        println("File: $fileName")
        println("Review Date: $timestamp")
        println("Number of suggestions: ${suggestions.size}")
        
        if (suggestions.isEmpty()) {
            println("\nNo suggestions found - code looks good!")
            return
        }

        println("\nDetailed Suggestions:")
        println("-------------------")
        suggestions.forEachIndexed { index, suggestion ->
            println("\n${index + 1}. Line ${suggestion.lineNumber}:")
            println("   Original code:")
            println("      ${suggestion.originalCode}")
            println("   Suggested change:")
            println("      ${suggestion.suggestion}")
            println("   Explanation:")
            println("      ${suggestion.explanation}")
            println("   ----------------------------------------")
        }
    }

    fun appendReviewComments(code: String, suggestions: List<CodeSuggestion>): String {
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