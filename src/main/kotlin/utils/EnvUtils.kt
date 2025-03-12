package utils

import io.github.cdimascio.dotenv.dotenv

internal object EnvUtils {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    internal fun getEnvVariable(key: String): String? = dotenv[key] ?: System.getenv(key)

    internal fun requireEnvVariable(key: String): String = getEnvVariable(key)
        ?: throw IllegalStateException("Required environment variable $key is not set")
}
