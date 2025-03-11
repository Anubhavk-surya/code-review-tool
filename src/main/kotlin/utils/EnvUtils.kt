package utils

import io.github.cdimascio.dotenv.dotenv

object EnvUtils {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    fun getEnvVariable(key: String): String? = dotenv[key] ?: System.getenv(key)

    fun requireEnvVariable(key: String): String = getEnvVariable(key)
        ?: throw IllegalStateException("Required environment variable $key is not set")
} 