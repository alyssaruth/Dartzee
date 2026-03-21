package dartzee.screen

import dartzee.core.bean.CheatBar
import dartzee.db.PlayerEntity
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.stats.player.PlayerAchievementsScreen
import java.util.concurrent.ConcurrentHashMap

object ScreenCache {
    private val hmGameIdToGameScreen = ConcurrentHashMap<String, AbstractDartsGameScreen>()

    // Embedded screens
    val hmClassToScreen = mutableMapOf<Class<out EmbeddedScreen>, EmbeddedScreen>()
    val mainScreen = DartsApp(CheatBar())

    fun getDartsGameScreens() = hmGameIdToGameScreen.values.distinct()

    inline fun <reified K : EmbeddedScreen> get() =
        hmClassToScreen.getOrPut(K::class.java) { K::class.java.getConstructor().newInstance() }
            as K

    fun currentScreen() = mainScreen.currentScreen

    inline fun <reified K : EmbeddedScreen> switch() {
        val screen = get<K>()
        switch(screen)
    }

    fun switch(scrn: EmbeddedScreen, reInit: Boolean = true) {
        mainScreen.switchScreen(scrn, reInit)
    }

    fun getDartsGameScreen(gameId: String) = hmGameIdToGameScreen[gameId]

    fun addDartsGameScreen(gameId: String, scrn: AbstractDartsGameScreen) {
        hmGameIdToGameScreen[gameId] = scrn
    }

    fun removeDartsGameScreen(scrn: AbstractDartsGameScreen) {
        val keys = hmGameIdToGameScreen.filter { it.value == scrn }.map { it.key }
        keys.forEach { hmGameIdToGameScreen.remove(it) }
    }

    fun emptyCache() {
        hmClassToScreen.clear()
        hmGameIdToGameScreen.clear()
    }

    fun switchToAchievementsScreen(player: PlayerEntity): PlayerAchievementsScreen {
        val scrn = PlayerAchievementsScreen(player)
        hmClassToScreen[PlayerAchievementsScreen::class.java] = scrn
        scrn.previousScrn = currentScreen()

        switch(scrn)

        return scrn
    }
}
