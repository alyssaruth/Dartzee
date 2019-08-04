package burlton.dartzee.test.screen.game

import burlton.dartzee.code.achievements.x01.AchievementX01BestGame
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertGame
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartsGameScreen: AbstractDartsTest()
{
    @Test
    fun `Should initialise correctly`()
    {
        val game = insertGame()
        val scrn = DartsGameScreen(game, 4)

        ScreenCache.getDartsGameScreen(game.rowId) shouldBe scrn
        scrn.title shouldBe "Game #${game.localId} (501 - 4 players)"
    }

    @Test
    fun `Should pass through to its gamePanel`()
    {
        val game = insertGame()
        val scrn = DartsGameScreen(game, 4)

        scrn.gamePanel = mockk(relaxed = true)

        scrn.fireAppearancePreferencesChanged()
        verify { scrn.gamePanel.fireAppearancePreferencesChanged() }

        val achievement = AchievementX01BestGame()
        scrn.achievementUnlocked("foo", "bar", achievement)
        verify { scrn.gamePanel.achievementUnlocked("bar", achievement) }
    }
}