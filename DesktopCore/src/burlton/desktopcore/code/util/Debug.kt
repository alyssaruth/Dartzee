package burlton.desktopcore.code.util

import burlton.desktopcore.code.util.DebugOutput
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import kotlin.math.floor

object Debug
{
    const val SQL_PREFIX = "[SQL] "
    const val BUG_REPORT_ADDITONAL_INFO_LINE = "Additional Information:"
    const val SUCCESS_MESSAGE = "Email sent successfully"

    private const val ERROR_MESSAGE_DELAY_MILLIS: Long = 10000 //10s
    private const val MINIMUM_EMAIL_GAP_MILLIS: Long = 10000
    private val DATE_FORMAT: SimpleDateFormat = SimpleDateFormat("dd/MM HH:mm:ss.SSS")

    private val emailSyncObject = Any()
    private val loggerFactory = ThreadFactory { r -> Thread(r, "Logger") }
    private var logService = Executors.newFixedThreadPool(1, loggerFactory)
    var lastErrorMillis: Long = -1
    var lastEmailMillis: Long = -1
    private var output: DebugOutput? = null
    var positionLastEmailed = 0
    private var emailsSentInSuccession = 1

    //Config
    var sendingEmails = true
    var logToSystemOut = false
    var productDesc = ""
    var debugExtension: DebugExtension? = null

    fun appendSql(text: String, logging: Boolean)
    {
        append(SQL_PREFIX + text, logging)
    }

    fun append(text: String, logging: Boolean = true, includeDate: Boolean = true, emailSubject: String? = null)
    {
        if (!logging) return

        val logRunnable = Runnable { appendInCurrentThread(text, includeDate, emailSubject) }
        val threadName = Thread.currentThread().name
        if (threadName != "Logger")
        {
            logService.execute(logRunnable)
        }
        else
        {
            logRunnable.run()
        }
    }

    private fun appendInCurrentThread(text: String, includeDate: Boolean, emailSubject: String?)
    {
        val time = if (includeDate) getCurrentTimeForLogging() else ""

        output ?: println("NULL OUTPUT. Trying to log: $text")

        output?.append("\n" + time + text)
        if (logToSystemOut)
        {
            println(time + text)
        }

        if (emailSubject != null && shouldSendEmail())
        {
            sendContentsAsEmail(emailSubject)
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
        append(trace, true, false, makeEmailTitle(t, message))
    }

    private fun makeEmailTitle(t: Throwable, message: String): String
    {
        val truncatedMessage = message.truncate(50)
        var extraDetails = " ($productDesc)"
        val username = CoreRegistry.instance[CoreRegistry.INSTANCE_STRING_USER_NAME, ""]
        if (username != "")
        {
            extraDetails += " - $username"
        }

        return "$t - $truncatedMessage$extraDetails"
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

    /**
     * SQLException
     */
    fun logSqlException(query: StringBuilder, sqle: SQLException?)
    {
        logSqlException(query.toString(), sqle)
    }

    fun logSqlException(query: String, sqle: SQLException?)
    {
        append("Caught SQLException for query: $query")

        var childSqle = sqle
        while (childSqle != null)
        {
            append("\n----- SQLException -----")
            append("  SQL State:  " + childSqle.sqlState)
            append("  Error Code: " + childSqle.errorCode)
            append("  Message:    " + childSqle.message)
            stackTrace(childSqle)
            childSqle = childSqle.nextException
        }
    }

    fun getCurrentTimeForLogging() = "${DATE_FORMAT.format(System.currentTimeMillis())}   "

    private fun shouldSendEmail() = debugExtension != null && sendingEmails

    private fun sendContentsAsEmail(title: String)
    {
        var fullTitle = title

        try
        {
            synchronized(emailSyncObject)
            {
                if (!needToSendMoreLogs()) return

                val timeSinceLastEmail = System.currentTimeMillis() - lastEmailMillis
                if (timeSinceLastEmail < MINIMUM_EMAIL_GAP_MILLIS)
                {
                    val timeToSleep = MINIMUM_EMAIL_GAP_MILLIS - timeSinceLastEmail
                    append("Waiting $timeToSleep millis before sending logs...")
                    Thread.sleep(timeToSleep)
                    fullTitle += " (Part " + (emailsSentInSuccession + 1) + ")"
                    emailsSentInSuccession++
                }
                else
                {
                    emailsSentInSuccession = 1
                }

                val totalLogs = getCurrentLogs()
                val message = totalLogs.substring(positionLastEmailed)
                debugExtension?.sendEmail(fullTitle, message)

                appendInCurrentThread(SUCCESS_MESSAGE,true,null)

                positionLastEmailed += message.length
                lastEmailMillis = System.currentTimeMillis()
            }
        }
        catch (t: Throwable)
        {
            stackTraceSilently(t)
            sendingEmails = false

            debugExtension?.unableToEmailLogs()
        }
    }

    fun sendBugReport(description: String, replication: String): Boolean
    {
        var fullDescription = description
        try
        {
            val username = CoreRegistry.instance[CoreRegistry.INSTANCE_STRING_USER_NAME, ""]
            if (username != "")
            {
                fullDescription += " - $username"
            }

            val totalLogs = getCurrentLogs()
            var message = ""
            if (replication.isNotEmpty())
            {
                message += BUG_REPORT_ADDITONAL_INFO_LINE
                message += "\n\n"
                message += replication
                message += "\n--------------------------\n"
            }

            val logsToSend = totalLogs.substring(positionLastEmailed)
            message += logsToSend
            debugExtension?.sendEmail(fullDescription, message)
            append(SUCCESS_MESSAGE, true)
            positionLastEmailed += logsToSend.length
            emailsSentInSuccession++
        }
        catch (t: Throwable)
        {
            append("Unable to send Bug Report. Exceptions follow.")
            stackTraceSilently(t)
            return false
        }
        return true
    }

    private fun needToSendMoreLogs(): Boolean
    {
        val ta = getCurrentLogs()
        val m = ta.substring(positionLastEmailed)
        return !(m.contains(SUCCESS_MESSAGE) && m.length < 100)
    }

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