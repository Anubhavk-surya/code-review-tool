package utils

import java.io.File
import java.nio.file.Paths

object FileUtils {
    fun readFile(fileName: String): String {
        val currentDir = Paths.get("").toAbsolutePath().toString()
        val possibleLocations = listOf(
            File(currentDir, fileName),
            File(currentDir, "src/main/kotlin/$fileName"),
            File(currentDir, "src/main/kotlin/model/$fileName"),
            File(currentDir, "src/main/kotlin/service/$fileName"),
            File(currentDir, "src/main/kotlin/utils/$fileName"),
            File(currentDir, "src/main/kotlin/routes/$fileName")
        )
        
        val file = possibleLocations.find { it.exists() }
            ?: throw IllegalArgumentException("File $fileName not found in any of the source directories")
        
        return file.readText()
    }

    fun writeUpdatedFile(originalFileName: String, updatedContent: String) {
        val currentDir = Paths.get("").toAbsolutePath().toString()
        val updatedFileName = "reviewed_${originalFileName}"
        val file = File(currentDir, updatedFileName)
        file.writeText(updatedContent)
    }
}