package burlton.dartzee.code.utils

import burlton.core.code.util.*
import burlton.desktopcore.code.util.AbstractDesktopClient
import burlton.desktopcore.code.util.UpdateChecker

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

    override fun sendAsyncInSingleThread(message: MessageSenderParams)
    {
        Debug.stackTrace("Invalid method")
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
        UpdateChecker.checkForUpdates(FILE_NAME_DARTS, OnlineConstants.SERVER_PORT_NUMBER_DOWNLOAD_DARTS)
    }

    override fun isCommunicatingWithServer() = false
    override fun finishServerCommunication(){}
    override fun unableToConnect(){}
    override fun connectionLost(){}
    override fun goOffline(){}

}
