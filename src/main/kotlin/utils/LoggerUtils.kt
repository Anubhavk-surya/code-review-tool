package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory


internal object LoggerUtils {
    internal inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)
    internal fun logger(name: String): Logger = LoggerFactory.getLogger(name)
} 