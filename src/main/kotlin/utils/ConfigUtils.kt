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

    fun getEnvVariable(key: String): String? {
        // First try to get from system environment
        return System.getenv(key)
            // Then try to get from .env file
            ?: properties.getProperty(key)
    }

    fun requireEnvVariable(key: String): String {
        return getEnvVariable(key) 
            ?: throw IllegalStateException("Required environment variable $key is not set")
    }
} 