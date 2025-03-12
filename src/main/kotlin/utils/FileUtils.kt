package utils

import java.io.File
import java.nio.file.Paths

internal object FileUtils {
    internal fun readFile(fileName: String): String {
        val file = File(fileName)
        if (!file.exists()) {
            throw IllegalArgumentException("File $fileName not found")
        }
        return file.readText()
    }

    internal fun writeUpdatedFile(originalFileName: String, updatedContent: String) {
        val currentDir = Paths.get("").toAbsolutePath().toString()
        val fileName = File(originalFileName).name  // Extract just the filename without path
        val updatedFileName = "reviewed_${fileName}"
        val file = File(currentDir, updatedFileName)
        file.writeText(updatedContent)
    }
}
