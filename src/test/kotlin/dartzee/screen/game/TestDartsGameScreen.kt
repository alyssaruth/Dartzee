package dartzee.screen.game

import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.screen.ScreenCache
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartsGameScreen: AbstractTest()
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