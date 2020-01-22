package dartzee.utils

import dartzee.core.util.Debug
import dartzee.core.util.FileUtil
import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import com.sun.mail.smtp.SMTPTransport
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

const val LOG_FILENAME_PREFIX = "DebugLog"

/**
 * Class to handle sending emails from the client, which is all done via the Entropy Server
 */
object ClientEmailer
{
    val TEMP_DIRECTORY = System.getProperty("user.dir") + "\\temp"

    fun canSendEmail(): Boolean
    {
        if (DartsClient.logSecret.isEmpty())
        {
            DialogUtil.showErrorLater("Unable to send logs - password must be entered through Utilities screen")
            Debug.append("No logSecret - unable to send logs")
            return false
        }

        return true
    }

    fun sendClientEmail(subject: String, body: String): Boolean
    {
        if (!attemptToSendEmail(subject, body))
        {
            writeEmailToFile(subject, body)
            return false
        }

        return true
    }
    private fun attemptToSendEmail(subject: String, body: String): Boolean
    {
        if (!canSendEmail())
        {
            return false
        }

        return try
        {
            sendEmail(subject, body, "entropydebug@gmail.com", "entropyDebug", DartsClient.logSecret)
            true
        }
        catch (me: MessagingException)
        {
            Debug.stackTraceSilently(me)
            false
        }
    }

    private fun writeEmailToFile(subject: String, body: String)
    {
        val message = "$subject\n$body"
        val tempDir = File(TEMP_DIRECTORY)
        if (!tempDir.isDirectory && !tempDir.mkdirs())
        {
            Debug.append("Failed to create temp directory $tempDir")
            return
        }

        val fileName = "$tempDir/${LOG_FILENAME_PREFIX}_${System.currentTimeMillis()}.txt"
        File(fileName).writeText(message)
    }

    /**
     * Check for unsent logs in the temp directory. If we find some, spawn a background thread to try sending these
     * to the server again.
     */
    fun tryToSendUnsentLogs()
    {
        val tempDir = File(TEMP_DIRECTORY)
        if (!tempDir.isDirectory)
        {
            Debug.append("$TEMP_DIRECTORY does not exist, no logs to resend")
            return
        }

        val files = tempDir.listFiles { file ->
            val filename = file.name
            filename.startsWith(LOG_FILENAME_PREFIX) && filename.endsWith(".txt")
        }

        if (files.isEmpty())
        {
            Debug.append("There are no logs to resend in $TEMP_DIRECTORY")
            return
        }

        Debug.append("Found ${files.size} logs to send, will do this in background thread")
        val emailRunnable = Runnable { files.forEach{ resendFromDebugFile(it) } }

        Thread(emailRunnable, "LogResender").start()
    }

    private fun resendFromDebugFile(file: File)
    {
        val logStr = file.readText(StandardCharsets.UTF_8)
        val lines = logStr.lines().toMutableList()
        val subject = lines.removeAt(0)
        val body = lines.joinToString("\n")

        val success = attemptToSendEmail(subject, body)
        if (!success)
        {
            Debug.append("Failed to send ${file.name}, leaving file for next start-up")
        }
        else
        {
            Debug.append("Sent ${file.name} successfully")
            FileUtil.deleteFileIfExists(file.absolutePath)
        }
    }

    fun sendEmail(title: String, message: String, targetEmail: String,
                  fromUsername: String, fromPassword: String)
    {
        if (message.isEmpty())
        {
            Debug.append("Not sending email $title as body is empty")
            return
        }

        // Get a Properties object
        val props = System.getProperties()
        props.setProperty("mail.smtps.host", "smtp.gmail.com")
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        props.setProperty("mail.smtp.socketFactory.fallback", "false")
        props.setProperty("mail.smtp.port", "465")
        props.setProperty("mail.smtp.socketFactory.port", "465")
        props.setProperty("mail.smtps.auth", "true")

        val session = Session.getInstance(props, null)

        val msg = MimeMessage(session)

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail, false))
        msg.subject = title
        msg.sentDate = Date()
        msg.setText(message, "utf-8")

        val t = session.getTransport("smtps") as SMTPTransport

        t.connect("smtp.gmail.com", fromUsername, fromPassword)
        t.sendMessage(msg, msg.allRecipients)
        t.close()
    }
}
