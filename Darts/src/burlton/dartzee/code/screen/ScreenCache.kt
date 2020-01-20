package burlton.dartzee.code.screen

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.screen.ai.AIConfigurationDialog
import burlton.dartzee.code.screen.game.AbstractDartsGameScreen
import burlton.dartzee.code.screen.preference.PreferencesDialog
import burlton.dartzee.code.screen.reporting.ConfigureReportColumnsDialog
import burlton.dartzee.code.core.bean.CheatBar
import burlton.desktopcore.code.screen.DebugConsole
import burlton.desktopcore.code.util.DialogUtil
import javax.swing.JOptionPane

object ScreenCache
{
    private val hmGameIdToGameScreen = mutableMapOf<String, AbstractDartsGameScreen>()

    //Embedded screens
    private val hmClassToScreen = mutableMapOf<Class<out EmbeddedScreen>, EmbeddedScreen>()
    val mainScreen = DartsApp(CheatBar())

    //Dialogs
    private var humanCreationDialog: HumanCreationDialog? = null
    private var aiConfigurationDialog: AIConfigurationDialog? = null
    private var preferencesDialog: PreferencesDialog? = null
    private var configureReportColumnsDialog: ConfigureReportColumnsDialog? = null

    //Other
    val debugConsole = DebugConsole()

    fun getPlayerManagementScreen() = getScreen(PlayerManagementScreen::class.java)

    fun getDartsGameScreens() = hmGameIdToGameScreen.values

    fun <K : EmbeddedScreen> getScreen(screenClass: Class<K>): K
    {
        return hmClassToScreen.getOrPut(screenClass) { screenClass.getConstructor().newInstance() } as K
    }

    fun currentScreen() = mainScreen.currentScreen

    fun <K : EmbeddedScreen> switchScreen(screenClass: Class<K>)
    {
        val screen = getScreen(screenClass)
        switchScreen(screen)
    }

    fun switchScreen(scrn: EmbeddedScreen?, reInit: Boolean = true)
    {
        mainScreen.switchScreen(scrn, reInit)
    }

    fun getHumanCreationDialog(): HumanCreationDialog
    {
        if (humanCreationDialog == null)
        {
            humanCreationDialog = HumanCreationDialog()
        }

        humanCreationDialog!!.setLocationRelativeTo(mainScreen)
        return humanCreationDialog!!
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

    fun getDartsGameScreen(gameId: String): AbstractDartsGameScreen?
    {
        return hmGameIdToGameScreen[gameId]
    }

    fun addDartsGameScreen(gameId: String, scrn: AbstractDartsGameScreen)
    {
        if (gameId.isEmpty())
        {
            Debug.stackTrace("Trying to cache GameScreen with no gameId.")
            return
        }

        hmGameIdToGameScreen[gameId] = scrn
    }

    fun removeDartsGameScreen(scrn: AbstractDartsGameScreen)
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
