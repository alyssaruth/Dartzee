package dartzee.`object`

import dartzee.logging.CODE_JUST_UPDATED
import dartzee.logging.CODE_MEMORY_SETTINGS
import dartzee.logging.CODE_UNEXPECTED_ARGUMENT
import dartzee.logging.CODE_UPDATE_CHECK
import dartzee.preferences.Preferences
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.UpdateManager
import java.util.*

object DartsClient {
    var devMode = false
    var trueLaunch = false
    var operatingSystem = System.getProperty("os.name").lowercase(Locale.ENGLISH)
    var justUpdated = false
    var updateManager = UpdateManager

    fun parseProgramArguments(args: Array<String>) {
        args.forEach { parseProgramArgument(it) }
    }

    fun logArgumentState() {
        if (justUpdated) {
            logger.info(CODE_JUST_UPDATED, "Just updated to a new version")
        }

        val rt = Runtime.getRuntime()
        val maxMb = rt.maxMemory() / (1024 * 1024)
        val totalMb = rt.totalMemory() / (1024 * 1024)
        logger.info(CODE_MEMORY_SETTINGS, "Heap settings - Max [$maxMb MB], Total [$totalMb MB]")
    }

    private fun parseProgramArgument(arg: String) {
        when (arg) {
            "justUpdated" -> justUpdated = true
            "devMode" -> devMode = true
            "trueLaunch" -> trueLaunch = true
            else -> logger.warn(CODE_UNEXPECTED_ARGUMENT, "Unexpected program argument: $arg")
        }
    }

    fun isAppleOs() =
        operatingSystem.contains("mac", ignoreCase = true) ||
            operatingSystem.contains("darwin", ignoreCase = true)

    fun isWindowsOs() = operatingSystem.contains("windows", ignoreCase = true)

    fun checkForUpdatesIfRequired() {
        if (devMode) {
            logger.info(CODE_UPDATE_CHECK, "Not checking for updates: I'm in dev mode")
            return
        }

        if (justUpdated) {
            logger.info(CODE_UPDATE_CHECK, "Not checking for updates: just updated")
            return
        }

        if (!preferenceService.get(Preferences.checkForUpdates)) {
            logger.info(CODE_UPDATE_CHECK, "Not checking for updates: preference disabled")
            return
        }

        updateManager.checkForUpdates(DARTS_VERSION_NUMBER)
    }
}
