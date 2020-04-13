package dartzee.logging

import dartzee.utils.InjectedThings

object Logger
{
    val destinations = mutableListOf<ILogDestination>()

    fun logSql(sqlStatement: String, genericStatement: String, duration: Long)
    {
        val message = "(${duration}ms) $sqlStatement"
        logInfo(CODE_SQL, message, KEY_DURATION to duration, KEY_GENERIC_SQL to genericStatement)
    }

    fun logInfo(code: LoggingCode, message: String, vararg keyValuePairs: Pair<Any, Any?>)
    {
        log(Severity.INFO, code, message, null, mapOf(*keyValuePairs))
    }

    fun logError(code: LoggingCode, message: String, errorObject: Throwable, vararg keyValuePairs: Pair<Any, Any?>)
    {
        log(Severity.ERROR, code, message, errorObject, mapOf(*keyValuePairs))
    }

    private fun log(severity: Severity, code: LoggingCode, message: String, errorObject: Throwable?, keyValuePairs: Map<Any, Any?>)
    {
        val timestamp = InjectedThings.clock.instant()
        val logRecord = LogRecord(timestamp, severity, code, message, errorObject, keyValuePairs)

        destinations.forEach { it.log(logRecord) }
    }
}