package burlton.dartzee.code.`object`

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.utils.*
import java.util.*

object DartsClient
{
    var derbyDbName = DartsDatabaseUtil.DATABASE_NAME
    var devMode = false
    var traceReadSql = true
    var traceWriteSql = true
    var logSecret: String = PreferenceUtil.getStringValue(PREFERENCES_STRING_LOG_SECRET)
    var sqlMaxDuration = MAX_SQL_DURATION
    var operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
    var justUpdated = false
    var updateManager = UpdateManager

    fun parseProgramArguments(args: Array<String>)
    {
        args.forEach { parseProgramArgument(it) }

        if (devMode) Debug.appendBanner("Running in dev mode")
        if (justUpdated) Debug.append("I've just updated")
        if (!logSecret.isEmpty()) Debug.append("logSecret is present - will email diagnostics")
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