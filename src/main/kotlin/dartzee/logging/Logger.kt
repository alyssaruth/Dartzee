package dartzee.logging

import dartzee.utils.InjectedThings
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

private const val LOGGER_THREAD = "Logger"

class Logger(private val destinations: List<ILogDestination>)
{
    private val loggingContext = mutableMapOf<String, Any?>()
    private val loggerFactory = ThreadFactory { r -> Thread(r, LOGGER_THREAD) }
    private var logService = Executors.newFixedThreadPool(1, loggerFactory)

    fun addToContext(loggingKey: String, value: Any?)
    {
        loggingContext[loggingKey] = value
    }

    fun logSql(sqlStatement: String, genericStatement: String, duration: Long)
    {
        val message = "(${duration}ms) $sqlStatement"
        info(CODE_SQL, message, KEY_DURATION to duration, KEY_GENERIC_SQL to genericStatement, KEY_SQL to sqlStatement)
    }

    fun info(code: LoggingCode, message: String, vararg keyValuePairs: Pair<String, Any?>)
    {
        log(Severity.INFO, code, message, null, mapOf(*keyValuePairs))
    }

    fun warn(code: LoggingCode, message: String, vararg keyValuePairs: Pair<String, Any?>)
    {
        log(Severity.WARN, code, message, null, mapOf(*keyValuePairs))
    }

    fun error(code: LoggingCode, message: String, errorObject: Throwable, vararg keyValuePairs: Pair<String, Any?>)
    {
        log(Severity.ERROR, code, message, errorObject, mapOf(*keyValuePairs, KEY_EXCEPTION_MESSAGE to errorObject.message))
    }

    private fun log(severity: Severity, code: LoggingCode, message: String, errorObject: Throwable?, keyValuePairs: Map<String, Any?>)
    {
        val timestamp = InjectedThings.clock.instant()
        val logRecord = LogRecord(timestamp, severity, code, message, errorObject, loggingContext + keyValuePairs)

        val runnable = Runnable { destinations.forEach { it.log(logRecord) } }
        if (Thread.currentThread().name != LOGGER_THREAD)
        {
            logService.execute(runnable)
        }
        else
        {
            runnable.run()
        }
    }

    fun waitUntilLoggingFinished()
    {
        try
        {
            logService.shutdown()
            logService.awaitTermination(30, TimeUnit.SECONDS)
        }
        catch (ie: InterruptedException) { }
        finally
        {
            logService = Executors.newFixedThreadPool(1, loggerFactory)
        }
    }
}