package dartzee.screen

import dartzee.core.bean.CheatBar
import dartzee.core.screen.DebugConsole
import dartzee.core.util.Debug
import dartzee.core.util.DialogUtil
import dartzee.logging.LoggingConsole
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.preference.PreferencesDialog
import dartzee.screen.reporting.ConfigureReportColumnsDialog
import javax.swing.JOptionPane
import kotlin.system.exitProcess

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
    val loggingConsole = LoggingConsole()

    fun getPlayerManagementScreen() = getScreen(PlayerManagementScreen::class.java)

    fun getDartsGameScreens() = hmGameIdToGameScreen.values.distinct()

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

    fun switchScreen(scrn: EmbeddedScreen, reInit: Boolean = true)
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

        exitProcess(0)
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
