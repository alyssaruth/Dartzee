package dartzee.screen.game

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.clickButton
import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestDartsGameScreen : AbstractTest() {
    @Test
    fun `Should initialise correctly`() {
        val game = insertGame()
        val scrn =
            DartsGameScreen(
                game,
                listOf(makeSingleParticipant(), makeSingleParticipant(), makeSingleParticipant())
            )

        ScreenCache.getDartsGameScreen(game.rowId) shouldBe scrn
        scrn.title shouldBe "Game #${game.localId} (501 - 3 players)"
    }

    @Test
    fun `Should pass through to its gamePanel`() {
        val game = insertGame()
        val scrn = DartsGameScreen(game, listOf(makeSingleParticipant()))

        scrn.gamePanel = mockk(relaxed = true)

        scrn.fireAppearancePreferencesChanged()
        verify { scrn.gamePanel.fireAppearancePreferencesChanged() }

        val achievement = AchievementX01BestGame()
        scrn.achievementUnlocked("foo", "bar", achievement)
        verify { scrn.gamePanel.achievementUnlocked("bar", achievement) }
    }

    @Test
    fun `Should show the tutorial if in party mode, and not start a new game`() {
        InjectedThings.partyMode = true

        val game = insertGame()
        val scrn = DartsGameScreen(game, listOf(makeSingleParticipant()))
        scrn.gamePanel = mockk(relaxed = true)
        scrn.startNewGame()

        scrn.findChild<DartsGamePanel<*, *>>() shouldBe null
        scrn.findChild<TutorialPanel>() shouldNotBe null
        verifyNotCalled { scrn.gamePanel.startNewGame(any()) }
    }

    @Test
    fun `Should start the game when tutorial ready button is pressed`() {
        InjectedThings.partyMode = true

        val participants = listOf(makeSingleParticipant())

        val game = insertGame()
        val scrn = DartsGameScreen(game, participants)
        scrn.gamePanel = mockk(relaxed = true)
        scrn.startNewGame()

        scrn.getChild<TutorialPanel>().clickButton(text = "I'm ready - let's play!")
        verify { scrn.gamePanel.startNewGame(participants) }

        scrn.findChild<TutorialPanel>().shouldBeNull()
        scrn.findChild<DartsGamePanel<*, *>>().shouldNotBeNull()
    }
}
