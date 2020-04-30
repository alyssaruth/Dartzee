package dartzee.screen

import dartzee.core.bean.CheatBar
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.preference.PreferencesDialog
import dartzee.screen.reporting.ConfigureReportColumnsDialog

object ScreenCache
{
    private val hmGameIdToGameScreen = mutableMapOf<String, AbstractDartsGameScreen>()

    //Embedded screens
    val hmClassToScreen = mutableMapOf<Class<out EmbeddedScreen>, EmbeddedScreen>()
    val mainScreen = DartsApp(CheatBar())

    //Dialogs
    private var humanCreationDialog: HumanCreationDialog? = null
    private var aiConfigurationDialog: AIConfigurationDialog? = null
    private var preferencesDialog: PreferencesDialog? = null
    private var configureReportColumnsDialog: ConfigureReportColumnsDialog? = null

    fun getDartsGameScreens() = hmGameIdToGameScreen.values.distinct()

    inline fun <reified K : EmbeddedScreen> get(): K
    {
        return hmClassToScreen.getOrPut(K::class.java) { K::class.java.getConstructor().newInstance() } as K
    }

    fun currentScreen() = mainScreen.currentScreen

    inline fun <reified K : EmbeddedScreen> switch()
    {
        val screen = get<K>()
        switch(screen)
    }

    fun switch(scrn: EmbeddedScreen, reInit: Boolean = true)
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

    fun emptyCache()
    {
        hmClassToScreen.clear()
        hmGameIdToGameScreen.clear()

        humanCreationDialog = null
        aiConfigurationDialog = null
        preferencesDialog = null
        configureReportColumnsDialog = null
    }
}
