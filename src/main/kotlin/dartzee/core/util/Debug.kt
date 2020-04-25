package dartzee.core.util

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object Debug
{
    private const val ERROR_MESSAGE_DELAY_MILLIS: Long = 10000 //10s
    private val DATE_FORMAT: SimpleDateFormat = SimpleDateFormat("dd/MM HH:mm:ss.SSS")

    private val loggerFactory = ThreadFactory { r -> Thread(r, "Debug") }
    private var logService = Executors.newFixedThreadPool(1, loggerFactory)
    var lastErrorMillis: Long = -1
    private var output: DebugOutput? = null

    //Config
    var logToSystemOut = false
    var debugExtension: DebugExtension? = null

    private fun append(text: String)
    {
        val logRunnable = Runnable { appendInCurrentThread(text) }
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

    private fun appendInCurrentThread(text: String)
    {
        val time = ""

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
        append(trace)
    }

    private fun getCurrentTimeForLogging() = "${DATE_FORMAT.format(System.currentTimeMillis())}   "

    fun getCurrentLogs(): String = output?.getLogs() ?: ""

    fun initialise(output: DebugOutput)
    {
        Debug.output = output
    }
}