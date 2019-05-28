package burlton.dartzee.code.utils

import burlton.core.code.util.AbstractClient
import burlton.core.code.util.Debug
import burlton.core.code.util.MessageSender
import burlton.desktopcore.code.util.AbstractDesktopClient

class DesktopDartsClient : AbstractDesktopClient()
{
    override fun init()
    {
        super.init()

        AbstractClient.derbyDbName = DartsDatabaseUtil.DATABASE_NAME
    }

    override fun getUsername(): String?
    {
        Debug.stackTrace("Invalid method")
        return null
    }

    override fun isOnline(): Boolean
    {
        Debug.append("Calling isOnline() for Dartzee - this is odd, but possible if retrying CRC check.")
        return true
    }

    override fun sendSyncOnDevice(runnable: MessageSender): String?
    {
        return runnable.sendMessage()
    }

    override fun handleResponse(message: String, encryptedResponse: String)
    {
        Debug.stackTrace("Invalid method")
    }

    override fun checkForUpdates()
    {
        UpdateManager.checkForUpdates(DARTS_VERSION_NUMBER)
    }

    override fun isCommunicatingWithServer() = false
    override fun finishServerCommunication(){}
    override fun unableToConnect(){}
    override fun connectionLost(){}
    override fun goOffline(){}

}
