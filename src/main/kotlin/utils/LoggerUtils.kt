package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Utility object for creating SLF4J loggers.
 * Usage: private val logger = LoggerUtils.logger()
 */
internal object LoggerUtils {
    /**
     * Creates a logger for the calling class.
     * @return SLF4J Logger instance
     */
    internal inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)
    
    /**
     * Creates a logger with a specific name.
     * @param name The name for the logger
     * @return SLF4J Logger instance
     */
    internal fun logger(name: String): Logger = LoggerFactory.getLogger(name)
} 