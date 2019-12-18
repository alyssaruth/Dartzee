package burlton.dartzee.code.screen

import burlton.core.code.util.Debug
import burlton.dartzee.code.screen.ai.AIConfigurationDialog
import burlton.dartzee.code.screen.game.AbstractDartsGameScreen
import burlton.dartzee.code.screen.preference.PreferencesDialog
import burlton.dartzee.code.screen.reporting.ConfigureReportColumnsDialog
import burlton.desktopcore.code.bean.CheatBar
import burlton.desktopcore.code.screen.DebugConsole
import burlton.desktopcore.code.util.DialogUtil
import javax.swing.JOptionPane

object ScreenCache
{
    private val hmGameIdToGameScreen = mutableMapOf<String, AbstractDartsGameScreen>()

    //Embedded screens
    private val hmClassToScreen = mutableMapOf<Class<out EmbeddedScreen>, EmbeddedScreen>()
    private var mainScreen: DartsApp? = null

    //Dialogs
    private var humanCreationDialog: HumanCreationDialog? = null
    private var aiConfigurationDialog: AIConfigurationDialog? = null
    private var preferencesDialog: PreferencesDialog? = null
    private var configureReportColumnsDialog: ConfigureReportColumnsDialog? = null

    //Other
    private var debugConsole: DebugConsole? = null

    @JvmStatic fun getPlayerManagementScreen() = getScreen(PlayerManagementScreen::class.java)

    fun getDartsGameScreens() = hmGameIdToGameScreen.values

    @JvmStatic fun <K : EmbeddedScreen> getScreen(screenClass: Class<K>): K
    {
        var scrn: K? = hmClassToScreen[screenClass] as K?

        try
        {
            if (scrn == null)
            {
                scrn = screenClass.newInstance()
                hmClassToScreen[screenClass] = scrn
            }
        }
        catch (iae: IllegalAccessException)
        {
            Debug.stackTrace(iae)
            DialogUtil.showError("Error loading screen.")
        }
        catch (iae: InstantiationException)
        {
            Debug.stackTrace(iae)
            DialogUtil.showError("Error loading screen.")
        }

        return scrn!!
    }

    fun getMainScreen(): DartsApp
    {
        if (mainScreen == null)
        {
            mainScreen = DartsApp(CheatBar())
        }

        return mainScreen!!
    }

    fun currentScreen() = getMainScreen().currentScreen

    fun <K : EmbeddedScreen> switchScreen(screenClass: Class<K>)
    {
        val screen = getScreen(screenClass)
        switchScreen(screen)
    }

    @JvmOverloads
    @JvmStatic
    fun switchScreen(scrn: EmbeddedScreen?, reInit: Boolean = true)
    {
        getMainScreen().switchScreen(scrn, reInit)
    }

    @JvmStatic fun getAIConfigurationDialog(): AIConfigurationDialog
    {
        if (aiConfigurationDialog == null)
        {
            aiConfigurationDialog = AIConfigurationDialog()
        }

        aiConfigurationDialog!!.setLocationRelativeTo(getMainScreen())
        return aiConfigurationDialog!!
    }

    fun getHumanCreationDialog(): HumanCreationDialog
    {
        if (humanCreationDialog == null)
        {
            humanCreationDialog = HumanCreationDialog()
        }

        humanCreationDialog!!.setLocationRelativeTo(getMainScreen())
        return humanCreationDialog!!
    }

    fun getDebugConsole(): DebugConsole
    {
        if (debugConsole == null)
        {
            debugConsole = DebugConsole()
        }

        return debugConsole!!
    }

    fun getPreferencesDialog(): PreferencesDialog
    {
        if (preferencesDialog == null)
        {
            preferencesDialog = PreferencesDialog()
        }

        preferencesDialog!!.init()
        return preferencesDialog!!
    }

    @JvmStatic fun getDartsGameScreen(gameId: String): AbstractDartsGameScreen?
    {
        return hmGameIdToGameScreen[gameId]
    }

    @JvmStatic fun addDartsGameScreen(gameId: String, scrn: AbstractDartsGameScreen)
    {
        if (gameId.isEmpty())
        {
            Debug.stackTrace("Trying to cache GameScreen with no gameId.")
            return
        }

        hmGameIdToGameScreen[gameId] = scrn
    }

    @JvmStatic fun removeDartsGameScreen(scrn: AbstractDartsGameScreen)
    {
        val keys = hmGameIdToGameScreen.filter { it.value == scrn }.map { it.key }
        keys.forEach { hmGameIdToGameScreen.remove(it) }
    }

    fun getConfigureReportColumnsDialog(): ConfigureReportColumnsDialog
    {
        if (configureReportColumnsDialog == null)
        {
            configureReportColumnsDialog = ConfigureReportColumnsDialog()
        }

        return configureReportColumnsDialog!!
    }

    fun exitApplication()
    {
        val openGames = getDartsGameScreens()
        val size = openGames.size
        if (size > 0)
        {
            val ans = DialogUtil.showQuestion("Are you sure you want to exit? There are $size game window(s) still open.", false)
            if (ans == JOptionPane.NO_OPTION)
            {
                return
            }
        }

        System.exit(0)
    }

    fun emptyCache()
    {
        hmClassToScreen.clear()

        humanCreationDialog = null
        aiConfigurationDialog = null
        preferencesDialog = null
        configureReportColumnsDialog = null
    }
}
