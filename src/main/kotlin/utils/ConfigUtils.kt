package utils

import java.io.File
import java.util.Properties

object ConfigUtils {
    private val properties = Properties()

    init {
        loadEnvFile()
    }

    private fun loadEnvFile() {
        val envFile = File(".env")
        if (envFile.exists()) {
            envFile.inputStream().use { stream ->
                properties.load(stream)
            }
        }
    }

    private fun getEnvVariable(key: String): String? {
        return System.getenv(key)
            ?: properties.getProperty(key)
    }

    fun requireEnvVariable(key: String): String {
        return getEnvVariable(key) 
            ?: throw IllegalStateException("Required environment variable $key is not set")
    }
} 