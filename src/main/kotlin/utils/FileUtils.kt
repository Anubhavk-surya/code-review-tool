package utils

import java.io.File
import java.nio.file.Paths

internal object FileUtils {
    private val supportedLanguages = mapOf(
        "kotlin" to ".kt",
        "cpp" to ".cpp",
        "java" to ".java"
    )

    internal fun checkLanguage(fileName: String, language: String) {
        if (!supportedLanguages.containsKey(language.lowercase())) {
            throw IllegalArgumentException("Unsupported language: $language")
        }
        
        val fileExtension = fileName.substringAfterLast('.')
        val expectedExtension = supportedLanguages[language.lowercase()]
        if (expectedExtension != null && !fileName.endsWith(expectedExtension)) {
            throw IllegalArgumentException("File extension '$fileExtension' does not match the expected extension for $language.")
        }
    }

    internal fun readFile(fileName: String, language: String): String {
        checkLanguage(fileName, language)
        val file = File(fileName)
        if (!file.exists()) {
            throw IllegalArgumentException("File $fileName not found")
        }
        return file.readText()
    }

    internal fun writeUpdatedFile(originalFileName: String, updatedContent: String) {
        val currentDir = Paths.get("").toAbsolutePath().toString()
        val fileName = File(originalFileName).name
        val updatedFileName = "reviewed_${fileName}"
        val file = File(currentDir, updatedFileName)
        file.writeText(updatedContent)
    }
}
