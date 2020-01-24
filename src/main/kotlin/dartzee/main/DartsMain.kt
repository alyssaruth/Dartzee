package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.*
import dartzee.screen.ScreenCache
import dartzee.utils.ClientEmailer
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.DartsDebugExtension
import javax.swing.JOptionPane
import javax.swing.UIManager

fun main(args: Array<String>)
{
    Debug.initialise(ScreenCache.debugConsole)
    checkForUserName()
    DialogUtil.init(MessageDialogFactory())

    setLookAndFeel()

    Debug.debugExtension = DartsDebugExtension()
    Debug.productDesc = "Darts $DARTS_VERSION_NUMBER"
    Debug.logToSystemOut = true

    val mainScreen = ScreenCache.mainScreen
    Thread.setDefaultUncaughtExceptionHandler(DebugUncaughtExceptionHandler())

    DartsClient.parseProgramArguments(args)

    Debug.sendingEmails = !DartsClient.devMode
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
