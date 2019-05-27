package burlton.desktopcore.code.util

import burlton.core.code.util.*
import org.w3c.dom.Document
import java.io.File
import java.nio.charset.StandardCharsets

private const val ROOT_TAG = "ClientMail"
private const val LOG_FILENAME_PREFIX = "DebugLog"
private const val SO_TIMEOUT_MILLIS = 60000 //1 minute

/**
 * Class to handle sending emails from the client, which is all done via the Entropy Server
 */
object ClientEmailer
{
    private val TEMP_DIRECTORY = System.getProperty("user.dir") + "\\temp"

    fun sendClientEmail(subject: String, body: String, containsCodeLines: Boolean)
    {
        val xml = factoryClientMailMessage(subject, body, containsCodeLines)
        val responseStr = sendEmailMessage(xml)
        if (responseStr == null)
        {
            Debug.append("Failed to send client log, details follow:")
            Debug.appendWithoutDate("Subject: $subject")
            Debug.newLine()

            writeEmailToFile(xml)

            throw Exception("Failed to send email")
        }
    }

    private fun writeEmailToFile(xmlMessage: Document)
    {
        val xmlStr = XmlUtil.getStringFromDocument(xmlMessage)
        val tempDir = File(TEMP_DIRECTORY)
        if (!tempDir.isDirectory && !tempDir.mkdirs())
        {
            Debug.append("Failed to create temp directory $tempDir")
            return
        }

        val fileName = LOG_FILENAME_PREFIX + System.currentTimeMillis() + ".txt"
        FileUtil.createNewFile(TEMP_DIRECTORY + "\\" + fileName, xmlStr)
    }

    /**
     * Write out the XML for a client mail message
     */
    private fun factoryClientMailMessage(subject: String, body: String, obfuscated: Boolean): Document
    {
        val message = XmlUtil.factoryNewDocument()
        val root = message!!.createElement(ROOT_TAG)

        val symmetricKey = KeyGeneratorUtil.generateSymmetricKey()
        val symmetricKeyString = EncryptionUtil.convertSecretKeyToString(symmetricKey!!)
        val encryptedKey = EncryptionUtil.encrypt(symmetricKeyString, MessageUtil.publicKey, true)

        root.setAttribute("EncryptedKey", encryptedKey)
        root.setAttribute("Subject", EncryptionUtil.encrypt(subject, symmetricKey))
        root.setAttribute("Body", EncryptionUtil.encrypt(body, symmetricKey))
        XmlUtil.setAttributeBoolean(root, "Obfuscated", obfuscated)

        message.appendChild(root)
        return message
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
        val xmlStr = file.readText(StandardCharsets.UTF_8)
        val xml = XmlUtil.getDocumentFromXmlString(xmlStr)
        val responseStr = sendEmailMessage(xml)
        if (responseStr == null)
        {
            Debug.append("Failed to send ${file.name}, leaving file for next start-up")
        }
        else
        {
            Debug.append("Sent ${file.name} successfully")
            FileUtil.deleteFileIfExists(file.absolutePath)
        }
    }

    private fun sendEmailMessage(xml: Document?): String?
    {
        //Send with a longer timeout as the server may take some time to send the email. Don't retry forever though.
        return AbstractClient.getInstance().sendSync(xml, false, SO_TIMEOUT_MILLIS, false)
    }
}
