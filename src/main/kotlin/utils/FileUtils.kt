package utils

import java.io.File
import java.nio.file.Paths

object FileUtils {
    fun readFile(fileName: String): String {
        val file = File(fileName)
        if (!file.exists()) {
            throw IllegalArgumentException("File $fileName not found")
        }
        return file.readText()
    }

    fun writeUpdatedFile(originalFileName: String, updatedContent: String) {
        val currentDir = Paths.get("").toAbsolutePath().toString()
        val fileName = File(originalFileName).name  // Extract just the filename without path
        val updatedFileName = "reviewed_${fileName}"
        val file = File(currentDir, updatedFileName)
        file.writeText(updatedContent)
    }
}