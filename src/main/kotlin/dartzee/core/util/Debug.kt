package dartzee.core.util

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import kotlin.math.floor

object Debug
{
    const val SQL_PREFIX = "[SQL] "

    private const val ERROR_MESSAGE_DELAY_MILLIS: Long = 10000 //10s
    private val DATE_FORMAT: SimpleDateFormat = SimpleDateFormat("dd/MM HH:mm:ss.SSS")

    private val loggerFactory = ThreadFactory { r -> Thread(r, "Debug") }
    private var logService = Executors.newFixedThreadPool(1, loggerFactory)
    var lastErrorMillis: Long = -1
    private var output: DebugOutput? = null

    //Config
    var logToSystemOut = false
    var debugExtension: DebugExtension? = null

    fun append(text: String, logging: Boolean = true, includeDate: Boolean = true)
    {
        if (!logging) return

        val logRunnable = Runnable { appendInCurrentThread(text, includeDate) }
        val threadName = Thread.currentThread().name
        if (threadName != "Debug")
        {
            logService.execute(logRunnable)
        }
        else
        {
            logRunnable.run()
        }
    }

    private fun appendInCurrentThread(text: String, includeDate: Boolean)
    {
        val time = if (includeDate) getCurrentTimeForLogging() else ""

        output ?: println("NULL OUTPUT. Trying to log: $text")

        output?.append("\n" + time + text)
        if (logToSystemOut)
        {
            println(time + text)
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

    fun appendWithoutDate(text: String, logging: Boolean = true)
    {
        append("                                      $text", logging, false)
    }

    fun appendTabbed(text: String)
    {
        appendWithoutDate("	$text")
    }

    fun appendBanner(text: String, logging: Boolean = true)
    {
        val length = text.length
        val starStr = (0 until length + 4).joinToString("") { "*" }

        append(starStr, logging)
        append("* $text *", logging)
        append(starStr, logging)
    }

    /**
     * Stack Trace methods
     */
    fun stackTrace(message: String)
    {
        stackTrace(Throwable(), message)
    }
    fun stackTrace(t: Throwable = Throwable(),  message: String = "", suppressError: Boolean = false)
    {
        if (debugExtension != null
            && !suppressError)
        {
            val showError = System.currentTimeMillis() - lastErrorMillis > ERROR_MESSAGE_DELAY_MILLIS
            debugExtension?.exceptionCaught(showError)
            if (showError)
            {
                lastErrorMillis = System.currentTimeMillis()
            }
        }

        val datetime = getCurrentTimeForLogging()
        var trace = ""
        if (message != "") {
            trace += datetime + message + "\n"
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        trace += datetime + sw.toString()
        append(trace, true, false)
    }

    fun stackTraceSilently(message: String)
    {
        stackTraceSilently(Throwable(message))
    }

    fun stackTraceSilently(t: Throwable)
    {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        val trace = sw.toString()
        t.printStackTrace()
        append(trace, true)
    }

    fun newLine()
    {
        appendWithoutDate("")
    }

    fun logProgress(workDone: Long, workToDo: Long, percentageToLogAt: Int)
    {
        //Convert 1 to 0.01, 50 to 0.5, etc.
        val percentageAsDecimal = percentageToLogAt.toDouble() / 100
        val percentageOfTotal = floor(workToDo * percentageAsDecimal)
        val remainder = workDone % percentageOfTotal
        if (remainder == 0.0)
        {
            val percentStr = MathsUtil.getPercentage(workDone, workToDo)
            val logStr = "Done $workDone/$workToDo ($percentStr%)"
            append(logStr)
        }
    }

    fun getCurrentTimeForLogging() = "${DATE_FORMAT.format(System.currentTimeMillis())}   "

    fun getCurrentLogs(): String = output?.getLogs() ?: ""

    fun initialise(output: DebugOutput)
    {
        Debug.output = output
    }

    fun clearLogs()
    {
        waitUntilLoggingFinished()
        output?.clear()
    }
}