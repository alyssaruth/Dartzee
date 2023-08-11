package dartzee.logging

import dartzee.core.util.MathsUtil
import dartzee.db.SqlStatementType
import dartzee.utils.InjectedThings
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import kotlin.math.floor

private const val LOGGER_THREAD = "Logger"

class Logger(private val destinations: List<ILogDestination>)
{
    val loggingContext = ConcurrentHashMap<String, Any?>()
    private val loggerFactory = ThreadFactory { r -> Thread(r, LOGGER_THREAD) }
    private var logService = Executors.newFixedThreadPool(1, loggerFactory)

    fun addToContext(loggingKey: String, value: Any?)
    {
        loggingContext[loggingKey] = value ?: ""
        destinations.forEach { it.contextUpdated(loggingContext.toMap()) }
    }

    fun logSql(sqlStatement: String, genericStatement: String, duration: Long, rowCount: Int, dbName: String)
    {
        info(CODE_SQL, sqlStatement,
            KEY_DURATION to duration,
            KEY_GENERIC_SQL to genericStatement,
            KEY_SQL to sqlStatement,
            KEY_ROW_COUNT to rowCount,
            KEY_DATABASE_NAME to dbName,
            KEY_STATEMENT_TYPE to SqlStatementType.fromStatement(genericStatement).name)
    }

    fun logProgress(code: LoggingCode, workDone: Long, workToDo: Long, percentageToLogAt: Int = 10)
    {
        //Convert 1 to 0.01, 50 to 0.5, etc.
        val percentageAsDecimal = percentageToLogAt.toDouble() / 100
        val percentageOfTotal = floor(workToDo * percentageAsDecimal)
        val remainder = workDone % percentageOfTotal
        if (remainder == 0.0)
        {
            val percentStr = MathsUtil.getPercentage(workDone, workToDo)
            val logStr = "Done $workDone/$workToDo ($percentStr%)"
            info(code, logStr)
        }
    }

    fun info(code: LoggingCode, message: String, vararg keyValuePairs: Pair<String, Any?>)
    {
        log(Severity.INFO, code, message, null, mapOf(*keyValuePairs))
    }

    fun warn(code: LoggingCode, message: String, vararg keyValuePairs: Pair<String, Any?>)
    {
        log(Severity.WARN, code, message, null, mapOf(*keyValuePairs))
    }

    fun error(code: LoggingCode, message: String, errorObject: Throwable = Throwable(), vararg keyValuePairs: Pair<String, Any?>)
    {
        log(Severity.ERROR, code, message, errorObject, mapOf(*keyValuePairs, KEY_EXCEPTION_MESSAGE to errorObject.message))
    }

    fun logSqlException(sqlStatement: String, genericStatement: String, sqlException: SQLException)
    {
        error(CODE_SQL_EXCEPTION, "Caught SQLException for statement: $sqlStatement", sqlException,
                KEY_SQL to sqlStatement,
                KEY_GENERIC_SQL to genericStatement,
                KEY_ERROR_CODE to sqlException.errorCode,
                KEY_SQL_STATE to sqlException.sqlState)
    }

    private fun log(severity: Severity, code: LoggingCode, message: String, errorObject: Throwable?, keyValuePairs: Map<String, Any?>)
    {
        val timestamp = InjectedThings.clock.instant()
        val logRecord = LogRecord(timestamp, severity, code, message, errorObject, loggingContext + keyValuePairs)

        val runnable = Runnable { destinations.forEach { it.log(logRecord) } }
        if (Thread.currentThread().name != LOGGER_THREAD && !logService.isShutdown && !logService.isTerminated)
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
        catch (_: InterruptedException) { }
        finally
        {
            logService = Executors.newFixedThreadPool(1, loggerFactory)
        }
    }
}