package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.*
import dartzee.logging.USERNAME_SET
import dartzee.screen.ScreenCache
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.DartsDebugExtension
import dartzee.utils.InjectedThings.logger
import javax.swing.JOptionPane
import javax.swing.UIManager
import kotlin.system.exitProcess

fun main(args: Array<String>)
{
    DartsClient.parseProgramArguments(args)

    if (!DartsClient.trueLaunch)
    {
        Runtime.getRuntime().exec("cmd /c start javaw -Xms256m -Xmx512m -jar Dartzee.jar trueLaunch")
        exitProcess(0)
    }

    Debug.initialise(ScreenCache.debugConsole)
    checkForUserName()
    DialogUtil.init(MessageDialogFactory())

    setLookAndFeel()

    Debug.debugExtension = DartsDebugExtension()
    Debug.productDesc = "Darts $DARTS_VERSION_NUMBER"
    Debug.logToSystemOut = true

    val mainScreen = ScreenCache.mainScreen
    Thread.setDefaultUncaughtExceptionHandler(DebugUncaughtExceptionHandler())

    DartsClient.logArgumentState()

    DartsClient.checkForUpdatesIfRequired()

    mainScreen.isVisible = true
    mainScreen.init()
}

private fun checkForUserName()
{
    var userName: String? = CoreRegistry.instance.get(CoreRegistry.INSTANCE_STRING_USER_NAME, "")
    if (userName?.isNotEmpty() == true)
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
        logger.logInfo(USERNAME_SET, "$userName has set their username")
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
