package dartzee.e2e

import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.core.util.DateStatics
import dartzee.game.GameLaunchParams
import dartzee.game.GameLauncher
import dartzee.game.GameType
import dartzee.getRows
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.retrieveGame
import dartzee.helper.retrieveParticipant
import dartzee.`object`.Dart
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PreferenceUtil
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import javax.swing.SwingUtilities
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestGameLoadE2E : AbstractE2ETest() {
    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, false)
    }

    @Test
    fun `E2E - Game load and AI resume`() {
        val (winner, loser) = createPlayers()

        val params =
            GameLaunchParams(
                listOf(winner, loser),
                GameType.X01,
                DEFAULT_X01_CONFIG.toJson(),
                false
            )
        GameLauncher().launchNewGame(params)

        waitForAssertion { retrieveGame().isFinished() shouldBe true }

        val gameId = retrieveGame().rowId
        val originalGameScreen = ScreenCache.getDartsGameScreen(gameId)!!
        val loserProgress = originalGameScreen.getScorer("Loser").lblResult.text

        closeOpenGames()

        SwingUtilities.invokeAndWait { GameLauncher().loadAndDisplayGame(gameId) }

        val gameScreen = ScreenCache.getDartsGameScreen(gameId)!!
        gameScreen.shouldBeVisible()

        verifyGameLoadedCorrectly(gameScreen)

        gameScreen.toggleStats()

        val loserScorer = gameScreen.getScorer("Loser")
        loserScorer.lblResult.text shouldBe loserProgress
        loserScorer.shouldBePaused()
        loserScorer.resume()

        waitForAssertion { retrieveParticipant().dtFinished shouldNotBe DateStatics.END_OF_TIME }
    }

    private fun verifyGameLoadedCorrectly(gameScreen: AbstractDartsGameScreen) {
        val winnerScorer = gameScreen.getScorer("Winner")
        winnerScorer.lblResult.text shouldBe "9 Darts"
        val rows = winnerScorer.tableScores.getRows()
        rows[0].shouldContainExactly(Dart(20, 3), Dart(20, 3), Dart(20, 3), 321)
        rows[1].shouldContainExactly(Dart(20, 3), Dart(20, 3), Dart(20, 3), 141)
        rows[2].shouldContainExactly(Dart(20, 3), Dart(19, 3), Dart(12, 2), 0)
    }
}
