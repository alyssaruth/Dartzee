package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import dartzee.core.util.MessageDialogFactory
import dartzee.logging.LoggerUncaughtExceptionHandler
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun main(args: Array<String>)
{
    DartsClient.parseProgramArguments(args)

    if (!DartsClient.trueLaunch && DartsClient.isWindowsOs())
    {
        Runtime.getRuntime().exec("cmd /c start javaw -Xms256m -Xmx512m -jar Dartzee.jar trueLaunch")
        exitProcess(0)
    }

    DialogUtil.init(MessageDialogFactory())

    Thread.setDefaultUncaughtExceptionHandler(LoggerUncaughtExceptionHandler())
    setLoggingContextFields()

    setLookAndFeel()

    DartsClient.logArgumentState()
    DartsClient.checkForUpdatesIfRequired()

    InjectedThings.esDestination.startPosting()

    val mainScreen = ScreenCache.mainScreen
    mainScreen.isVisible = true
    mainScreen.init()
}

fun exitApplication()
{
    val openGames = ScreenCache.getDartsGameScreens()
    val size = openGames.size
    if (size > 0)
    {
        val ans = DialogUtil.showQuestion("Are you sure you want to exit? There are $size game window(s) still open.", false)
        if (ans == JOptionPane.NO_OPTION)
        {
            return
        }
    }

    InjectedThings.esDestination.shutDown()

    exitProcess(0)
}
