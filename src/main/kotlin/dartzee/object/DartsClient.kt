package dartzee.`object`

import dartzee.core.util.Debug
import dartzee.logging.CODE_JUST_UPDATED
import dartzee.logging.CODE_MEMORY_SETTINGS
import dartzee.logging.CODE_UNEXPECTED_ARGUMENT
import dartzee.utils.*
import dartzee.utils.InjectedThings.logger
import java.util.*

object DartsClient
{
    var derbyDbName = DartsDatabaseUtil.DATABASE_NAME
    var devMode = false
    var trueLaunch = false
    var operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
    var justUpdated = false
    var updateManager = UpdateManager

    fun parseProgramArguments(args: Array<String>)
    {
        args.forEach { parseProgramArgument(it) }
    }

    fun logArgumentState()
    {
        if (justUpdated)
        {
            logger.info(CODE_JUST_UPDATED, "Just updated to a new version")
        }

        val rt = Runtime.getRuntime()
        val maxMb = rt.maxMemory() / (1024*1024)
        val totalMb = rt.totalMemory() / (1024*1024)
        logger.info(CODE_MEMORY_SETTINGS, "Heap settings - Max [$maxMb MB], Total [$totalMb MB]")
    }

    private fun parseProgramArgument(arg: String)
    {
        when (arg)
        {
            "justUpdated" -> justUpdated = true
            "devMode" -> devMode = true
            "trueLaunch" -> trueLaunch = true
            else -> logger.warn(CODE_UNEXPECTED_ARGUMENT, "Unexpected program argument: $arg")
        }
    }

    fun isAppleOs() = operatingSystem.contains("mac") || operatingSystem.contains("darwin")

    fun checkForUpdatesIfRequired()
    {
        if (devMode)
        {
            Debug.append("Not checking for updates as I'm in dev mode")
            return
        }

        if (justUpdated)
        {
            Debug.append("Just updated - not checking for updates")
            return
        }

        if (!PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES))
        {
            Debug.append("Not checking for updates as preference is disabled")
            return
        }

        updateManager.checkForUpdates(DARTS_VERSION_NUMBER)
    }
}