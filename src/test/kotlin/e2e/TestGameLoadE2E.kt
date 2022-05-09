package e2e

import com.github.alexburlton.swingtest.awaitCondition
import com.github.alexburlton.swingtest.shouldBeVisible
import dartzee.`object`.Dart
import dartzee.game.GameLauncher
import dartzee.core.util.DateStatics
import dartzee.game.GameType
import dartzee.getRows
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.retrieveGame
import dartzee.helper.retrieveParticipant
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import javax.swing.SwingUtilities

class TestGameLoadE2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED, PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 0)
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, false)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Game load and AI resume`()
    {
        val (winner, loser) = createPlayers()

        GameLauncher().launchNewGame(listOf(winner, loser), GameType.X01, "501")

        awaitCondition { retrieveGame().isFinished() }

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

        awaitCondition { retrieveParticipant().dtFinished != DateStatics.END_OF_TIME }
    }

    private fun verifyGameLoadedCorrectly(gameScreen: AbstractDartsGameScreen)
    {
        val winnerScorer = gameScreen.getScorer("Winner")
        winnerScorer.lblResult.text shouldBe "9 Darts"
        val rows = winnerScorer.tableScores.getRows()
        rows[0].shouldContainExactly(Dart(20, 3), Dart(20, 3), Dart(20, 3), 321)
        rows[1].shouldContainExactly(Dart(20, 3), Dart(20, 3), Dart(20, 3), 141)
        rows[2].shouldContainExactly(Dart(20, 3), Dart(19, 3), Dart(12, 2), 0)
    }
}