package dartzee.`object`

import dartzee.core.util.Debug
import dartzee.utils.*
import java.util.*

object DartsClient
{
    var derbyDbName = DartsDatabaseUtil.DATABASE_NAME
    var devMode = false
    var traceReadSql = true
    var traceWriteSql = true
    var logSecret: String = PreferenceUtil.getStringValue(PREFERENCES_STRING_LOG_SECRET)
    var trueLaunch = false
    var sqlMaxDuration = MAX_SQL_DURATION
    var operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
    var justUpdated = false
    var updateManager = UpdateManager

    fun parseProgramArguments(args: Array<String>)
    {
        args.forEach { parseProgramArgument(it) }
    }

    fun logArgumentState()
    {
        Debug.appendBanner("Running in dev mode", devMode)
        Debug.append("I've just updated", justUpdated)
        Debug.append("logSecret is present - will email diagnostics", logSecret.isNotEmpty())

        val rt = Runtime.getRuntime()
        val maxMb = rt.maxMemory() / (1024*1024)
        val totalMb = rt.totalMemory() / (1024*1024)
        Debug.append("Heap settings - Max [$maxMb MB], Total [$totalMb MB]")
    }

    private fun parseProgramArgument(arg: String)
    {
        val argAndValue = arg.split("=")
        val argName = argAndValue[0]
        val argValue = if (argAndValue.size > 1) argAndValue[1] else ""

        when (argName)
        {
            "justUpdated" -> justUpdated = true
            "devMode" -> devMode = true
            "logSecret" -> logSecret = argValue
            "trueLaunch" -> trueLaunch = true
            else -> Debug.append("Unexpected program argument: $arg")
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