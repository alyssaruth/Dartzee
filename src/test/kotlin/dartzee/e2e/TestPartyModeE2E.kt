package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import dartzee.bean.GameSetupPlayerSelector
import dartzee.bean.ScrollTableDartsGame
import dartzee.clickButton
import dartzee.drtDoubleTwenty
import dartzee.drtInnerFourteen
import dartzee.drtInnerSeven
import dartzee.drtOuterFive
import dartzee.drtOuterFourteen
import dartzee.drtOuterNineteen
import dartzee.drtOuterOne
import dartzee.drtOuterTwenty
import dartzee.drtTrebleFive
import dartzee.drtTrebleNineteen
import dartzee.drtTrebleSeven
import dartzee.drtTrebleTwenty
import dartzee.game.GameLauncher
import dartzee.getRows
import dartzee.helper.preparePlayers
import dartzee.screen.DartsApp
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGamePanel
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.x01.GameStatisticsPanelX01
import dartzee.waitForAssertionWithReturn
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import javax.swing.JButton
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Test

class TestPartyModeE2E : AbstractE2ETest() {
    @Test
    fun `E2E - Party Mode`() {
        preparePlayers(2)

        val app = launchApp()
        app.clickButton(text = "Utilities")
        app.clickButton(text = "Enter Party Mode")
        app.clickButton(text = "New Game")

        val selector = app.getChild<GameSetupPlayerSelector>()
        selector.selectTopPlayer()
        selector.selectTopPlayer()
        app.clickButton(text = "Launch Game >")

        val gameWindow = waitForAssertionWithReturn {
            findWindow<DartsGameScreen>().run {
                shouldNotBeNull()
                shouldBeVisible()
                this
            }
        }

        gameWindow.clickButton(text = "I'm ready - let's play!")

        val gamePanel = gameWindow.getChild<DartsGamePanel<*, *>>()
        gamePanel.throwHumanRound(drtOuterTwenty(), drtTrebleTwenty(), drtOuterTwenty()) // 201
        gamePanel.throwHumanRound(drtOuterNineteen(), drtOuterNineteen(), drtInnerSeven()) // 256
        gamePanel.throwHumanRound(drtOuterTwenty(), drtTrebleTwenty(), drtTrebleFive()) // 106
        gamePanel.throwHumanRound(drtTrebleNineteen(), drtTrebleNineteen(), drtTrebleSeven()) // 121
        gamePanel.throwHumanRound(drtOuterTwenty(), drtOuterFive(), drtOuterOne()) // 80
        gamePanel.throwHumanRound(drtTrebleNineteen(), drtInnerFourteen(), drtOuterFourteen()) // 36
        gamePanel.throwHumanRound(drtOuterTwenty(), drtDoubleTwenty(), drtOuterTwenty()) // 0

        // Check game outcome
        gameWindow.getScorer("Alice").lblResult.text shouldBe "12 Darts"
        gameWindow.getScorer("Bob").lblResult.text shouldBe "Unfinished"

        val statsPane = gameWindow.getChild<GameStatisticsPanelX01>()
        statsPane.shouldBeVisible()
        val extraMessage = statsPane.getChild<JTextPane> { it.text.contains("Congrats") }
        extraMessage.text.shouldContain("Congrats to Alice on the win!")
        gameWindow.dispose()

        // Check leaderboard
        app.clickChild<JButton>(text = " < Back")
        app.clickChild<JButton>(text = "Leaderboards")

        val leaderboardTable = app.getChild<ScrollTableDartsGame>()
        leaderboardTable
            .getRows()
            .map { it.filterIndexed { index: Int, _ -> index != 1 } }
            .shouldContainExactly(arrayOf(1, "Alice", 1L, 12))

        // Reload the game
        SwingUtilities.invokeAndWait { GameLauncher().loadAndDisplayGame(gamePanel.getGameId()) }

        val loadedWindow = ScreenCache.getDartsGameScreen(gamePanel.getGameId())!!
        loadedWindow.shouldBeVisible()
        loadedWindow.getScorer("Alice").lblResult.text shouldBe "12 Darts"
        loadedWindow.getScorer("Bob").lblResult.text shouldBe "Unfinished"
    }

    private fun launchApp(): DartsApp {
        val mainScreen = ScreenCache.mainScreen
        mainScreen.isVisible = true
        mainScreen.init()
        return mainScreen
    }
}
