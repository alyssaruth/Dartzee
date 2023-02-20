package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import dartzee.core.util.EdtMonitor
import dartzee.core.util.MessageDialogFactory
import dartzee.logging.LoggerUncaughtExceptionHandler
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
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

    EdtMonitor.start()

    InjectedThings.esDestination.startPosting()

    val mainScreen = ScreenCache.mainScreen
    mainScreen.isVisible = true
    mainScreen.init()
}