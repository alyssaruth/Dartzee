package dartzee.logging

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

enum class Severity {
    INFO,
    WARN,
    ERROR
}

data class LogRecord(val timestamp: Instant,
                     val severity: Severity,
                     val loggingCode: LoggingCode,
                     val message: String,
                     val errorObject: Throwable?,
                     val keyValuePairs: Map<String, Any?>)
{
    private val dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())
            .format(timestamp)

    override fun toString() = "$dateStr   [$loggingCode] $message"

    fun getThrowableStr() = errorObject?.let { "$dateStr   ${extractStackTrace(errorObject)}" }
}