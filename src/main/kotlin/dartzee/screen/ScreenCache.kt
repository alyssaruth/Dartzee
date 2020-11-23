package dartzee.screen

import dartzee.core.bean.CheatBar
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.sync.SyncSummaryPanel

object ScreenCache
{
    private val hmGameIdToGameScreen = mutableMapOf<String, AbstractDartsGameScreen>()

    //Embedded screens
    val hmClassToScreen = mutableMapOf<Class<out EmbeddedScreen>, EmbeddedScreen>()
    val syncSummaryPanel = SyncSummaryPanel()
    val mainScreen = DartsApp(CheatBar())

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

    fun getDartsGameScreen(gameId: String) = hmGameIdToGameScreen[gameId]

    fun addDartsGameScreen(gameId: String, scrn: AbstractDartsGameScreen)
    {
        hmGameIdToGameScreen[gameId] = scrn
    }

    fun removeDartsGameScreen(scrn: AbstractDartsGameScreen)
    {
        val keys = hmGameIdToGameScreen.filter { it.value == scrn }.map { it.key }
        keys.forEach { hmGameIdToGameScreen.remove(it) }
    }

    fun emptyCache()
    {
        hmClassToScreen.clear()
        hmGameIdToGameScreen.clear()
    }
}
