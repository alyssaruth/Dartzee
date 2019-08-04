package burlton.dartzee.code.main

import burlton.core.code.util.CoreRegistry
import burlton.core.code.util.Debug
import burlton.core.code.util.DebugUncaughtExceptionHandler
import burlton.dartzee.code.`object`.DartsClient
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.utils.ClientEmailer
import burlton.dartzee.code.utils.DARTS_VERSION_NUMBER
import burlton.dartzee.code.utils.DartsDebugExtension
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.MessageDialogFactory
import javax.swing.JOptionPane
import javax.swing.UIManager

object DartsMain
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        Debug.initialise(ScreenCache.getDebugConsole())
        checkForUserName()
        DialogUtil.init(MessageDialogFactory())

        setLookAndFeel()

        Debug.setDebugExtension(DartsDebugExtension())
        Debug.setProductDesc("Darts $DARTS_VERSION_NUMBER")
        Debug.setLogToSystemOut(true)

        val mainScreen = ScreenCache.getMainScreen()
        Thread.setDefaultUncaughtExceptionHandler(DebugUncaughtExceptionHandler())

        DartsClient.parseProgramArguments(args)

        Debug.setSendingEmails(!DartsClient.devMode)
        ClientEmailer.tryToSendUnsentLogs()

        DartsClient.checkForUpdatesIfRequired()

        mainScreen.isVisible = true
        mainScreen.init()
    }

    private fun checkForUserName()
    {
        var userName: String? = CoreRegistry.instance.get(CoreRegistry.INSTANCE_STRING_USER_NAME, "")
        if (!userName!!.isEmpty())
        {
            return
        }

        Debug.append("Username isn't specified - will prompt for one now")
        while (userName == null || userName.isEmpty())
        {
            userName = JOptionPane.showInputDialog(null, "Please enter your name (for debugging purposes).\nThis will only be asked for once.", "Enter your name")
        }

        CoreRegistry.instance.put(CoreRegistry.INSTANCE_STRING_USER_NAME, userName)

        try
        {
            ClientEmailer.sendClientEmail("Username notification", "$userName has set their username.")
        }
        catch (t: Throwable)
        {
            //If there's no internet connection or Google does something dumb, just log a line
            Debug.append("Caught $t trying to send username notification.")
        }
    }

    private fun setLookAndFeel()
    {
        Debug.append("Initialising Look & Feel - Operating System: ${DartsClient.operatingSystem}")
        if (DartsClient.isAppleOs())
        {
            setLookAndFeel("javax.swing.plaf.metal")
        }
        else
        {
            setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        }
    }

    private fun setLookAndFeel(laf: String)
    {
        try
        {
            UIManager.setLookAndFeel(laf)
        }
        catch (e: Exception)
        {
            Debug.append("Failed to load LookAndFeel $laf. Caught $e")
            DialogUtil.showError("Failed to load Look & Feel 'Nimbus'.")
        }

    }
}
