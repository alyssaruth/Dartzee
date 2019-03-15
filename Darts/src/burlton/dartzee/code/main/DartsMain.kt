package burlton.dartzee.code.main

import burlton.core.code.util.AbstractClient
import burlton.core.code.util.Debug
import burlton.core.code.util.DebugUncaughtExceptionHandler
import burlton.core.code.util.OnlineConstants
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.utils.DartsDebugExtension
import burlton.dartzee.code.utils.DesktopDartsClient
import burlton.dartzee.code.utils.PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.MessageDialogFactory
import javax.swing.UIManager

object DartsMain
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        Debug.initialise(ScreenCache.getDebugConsole())
        AbstractClient.setInstance(DesktopDartsClient())
        DialogUtil.init(MessageDialogFactory())

        setLookAndFeel()

        Debug.setDebugExtension(DartsDebugExtension())
        Debug.setProductDesc("Darts " + OnlineConstants.DARTS_VERSION_NUMBER)
        Debug.setLogToSystemOut(true)

        val mainScreen = ScreenCache.getMainScreen()
        Thread.setDefaultUncaughtExceptionHandler(DebugUncaughtExceptionHandler())

        AbstractClient.parseProgramArguments(args)

        Debug.setSendingEmails(!AbstractClient.devMode)

        checkForUpdatesIfRequired()

        mainScreen.isVisible = true
        mainScreen.init()
    }

    private fun setLookAndFeel()
    {
        AbstractClient.setOs()
        Debug.append("Initialising Look & Feel - Operating System: " + AbstractClient.operatingSystem)
        if (AbstractClient.isAppleOs())
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

    private fun checkForUpdatesIfRequired()
    {
        if (AbstractClient.devMode)
        {
            Debug.append("Not checking for updates as I'm in dev mode")
            return
        }

        if (!PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES))
        {
            Debug.append("Not checking for updates as preference is disabled")
            return
        }

        AbstractClient.getInstance().checkForUpdatesIfRequired()
    }
}
