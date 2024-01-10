package dartzee.screen.player

import com.github.alyssaburlton.swingtest.doHover
import com.github.alyssaburlton.swingtest.doHoverAway
import dartzee.achievements.AchievementType
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.rtc.AchievementClockBestGame
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementsScreen
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TestPlayerAchievementsButton : AbstractTest() {
    @Test
    fun `Should have the correct text based on the players achievements`() {
        // 6, 1, 0
        val player = insertPlayer()
        val a1 =
            insertAchievement(
                playerId = player.rowId,
                type = AchievementType.X01_BEST_GAME,
                achievementCounter = AchievementX01BestGame().pinkThreshold
            )
        val a2 =
            insertAchievement(
                playerId = player.rowId,
                type = AchievementType.GOLF_BEST_GAME,
                achievementCounter = AchievementGolfBestGame().redThreshold
            )
        val a3 =
            insertAchievement(
                playerId = player.rowId,
                type = AchievementType.CLOCK_BEST_GAME,
                achievementCounter = AchievementClockBestGame().redThreshold + 1
            )

        val button = PlayerAchievementsButton(player, listOf(a1, a2, a3))

        button.text shouldBe
            "<html><center><h3>Achievements</h3> 7 / ${getAchievementMaximum()}</center></html>"
    }

    @Test
    fun `Should switch to the achievements screen on click`() {
        val startingScreen = ScreenCache.currentScreen()
        val player = insertPlayer()

        val button = PlayerAchievementsButton(player, listOf())
        val text = button.text
        button.doHover()
        button.doClick()

        button.text shouldBe text

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<PlayerAchievementsScreen>()
        currentScreen.player shouldBe player
        currentScreen.previousScrn shouldBe startingScreen
    }

    @Test
    fun `Should change text on hover`() {
        val player = insertPlayer()
        val button = PlayerAchievementsButton(player, listOf())
        val text = button.text

        button.doHover()
        button.text shouldBe "<html><h3>Achievements &gt;</h3></html>"

        button.doHoverAway()
        button.text shouldBe text
    }
}
