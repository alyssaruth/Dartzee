package dartzee.main

import dartzee.core.util.DialogUtil
import dartzee.core.util.EdtMonitor
import dartzee.core.util.MessageDialogFactory
import dartzee.logging.CODE_JAVA_UNSUPPORTED
import dartzee.logging.LoggerUncaughtExceptionHandler
import dartzee.`object`.DartsClient
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger

fun main(args: Array<String>) {
    DartsClient.parseProgramArguments(args)

    if (!DartsClient.trueLaunch && DartsClient.isWindowsOs()) {
        Runtime.getRuntime()
            .exec(
                "cmd /c start javaw -Xms256m -Xmx512m -jar Dartzee.jar trueLaunch ${args.joinToString(" ")}"
            )
        InjectedThings.exiter.exit(0)
    }

    Thread.setDefaultUncaughtExceptionHandler(LoggerUncaughtExceptionHandler())
    setLoggingContextFields()
    InjectedThings.esDestination.startPosting()

    setLookAndFeel()
    initialiseAnimations()

    DialogUtil.init(MessageDialogFactory())

    DartsClient.logArgumentState()

    if (versionTooOld()) {
        logger.error(CODE_JAVA_UNSUPPORTED, "Java version is not supported.")
        DialogUtil.showError(
            "Dartzee requires Java version 21 or higher to run. Please install the latest Java and try again."
        )

        InjectedThings.exiter.exit(1)
    }

    DartsClient.checkForUpdatesIfRequired()
    EdtMonitor.start()

    val mainScreen = ScreenCache.mainScreen
    mainScreen.isVisible = true
    mainScreen.init()
}

private fun versionTooOld() =
    try {
        Runtime.version().feature() < 11
    } catch (_: NoSuchMethodError) {
        true
    }
