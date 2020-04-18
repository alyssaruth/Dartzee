package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.CoreRegistry.INSTANCE_STRING_USER_NAME
import dartzee.core.util.CoreRegistry.instance
import dartzee.core.util.Debug
import dartzee.core.util.DebugUncaughtExceptionHandler
import dartzee.core.util.DialogUtil
import dartzee.core.util.MessageDialogFactory
import dartzee.logging.*
import dartzee.screen.ScreenCache
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.DartsDebugExtension
import dartzee.utils.InjectedThings.logger
import kotlin.system.exitProcess

fun main(args: Array<String>)
{
    DartsClient.parseProgramArguments(args)

    if (!DartsClient.trueLaunch && !DartsClient.isAppleOs())
    {
        Runtime.getRuntime().exec("cmd /c start javaw -Xms256m -Xmx512m -jar Dartzee.jar trueLaunch")
        exitProcess(0)
    }

    Debug.initialise(ScreenCache.debugConsole)
    DialogUtil.init(MessageDialogFactory())

    setLoggingContextFields()

    setLookAndFeel()

    Debug.debugExtension = DartsDebugExtension()
    Debug.logToSystemOut = true

    val mainScreen = ScreenCache.mainScreen
    Thread.setDefaultUncaughtExceptionHandler(DebugUncaughtExceptionHandler())

    DartsClient.logArgumentState()

    DartsClient.checkForUpdatesIfRequired()

    mainScreen.isVisible = true
    mainScreen.init()
}

private fun setLoggingContextFields()
{
    logger.addToContext(KEY_USERNAME, getUsername())
    logger.addToContext(KEY_APP_VERSION, DARTS_VERSION_NUMBER)
    logger.addToContext(KEY_OPERATING_SYSTEM, DartsClient.operatingSystem)
    logger.addToContext(KEY_DEVICE_ID, getDeviceId())
    logger.addToContext(KEY_DEV_MODE, DartsClient.devMode)
}
