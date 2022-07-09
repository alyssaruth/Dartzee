package e2e

import com.github.alexburlton.swingtest.getChild
import dartzee.game.GameLauncher
import com.github.alexburlton.swingtest.awaitCondition
import dartzee.core.util.DateStatics
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.retrieveDartsMatch
import dartzee.screen.ScreenCache
import dartzee.screen.game.MatchSummaryPanel
import dartzee.screen.game.scorer.MatchScorer
import dartzee.screen.game.x01.X01MatchScreen
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import javax.swing.JTabbedPane

class TestMatchE2E: AbstractRegistryTest()
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
    fun `E2E - Two game match`()
    {
        val match = insertDartsMatch(games = 2, matchParams = "", mode = MatchMode.FIRST_TO)
        match.gameType = GameType.X01
        match.gameParams = "501"

        val (winner, loser) = createPlayers()
        val launchParams = GameLaunchParams(listOf(winner, loser), GameType.X01, "501", false)
        GameLauncher().launchNewMatch(match, launchParams)

        awaitCondition { retrieveDartsMatch().dtFinish != DateStatics.END_OF_TIME }

        verifyDatabase(match.rowId, winner, loser)
        verifyUi()
    }

    private fun verifyDatabase(matchId: String, winner: PlayerEntity, loser: PlayerEntity)
    {
        val games = GameEntity().retrieveEntities()
        games.size shouldBe 2
        games.forEach {
            it.gameParams shouldBe "501"
            it.gameType shouldBe GameType.X01
            it.dtFinish shouldNotBe DateStatics.END_OF_TIME
            it.dartsMatchId shouldBe matchId
        }

        val gameIds = games.map { it.rowId }

        val participants = ParticipantEntity().retrieveEntities()
        val winnerPts = participants.filter { it.playerId == winner.rowId }
        winnerPts.forEach {
            it.finishingPosition shouldBe 1
            it.finalScore shouldBe 9
            it.dtFinished shouldNotBe DateStatics.END_OF_TIME
            gameIds.shouldContain(it.gameId)
        }

        winnerPts.map { it.ordinal }.shouldContainExactlyInAnyOrder(0, 1)

        val loserPts = participants.filter { it.playerId == loser.rowId }
        loserPts.forEach {
            it.finishingPosition shouldBe 2
            it.finalScore shouldBe -1
            it.dtFinished shouldBe DateStatics.END_OF_TIME
            gameIds.shouldContain(it.gameId)
        }
    }

    private fun verifyUi()
    {
        val matchScreen = ScreenCache.getDartsGameScreens().first() as X01MatchScreen
        matchScreen.title shouldBe "Match #1 (First to 2 - 501)"

        val summaryPanel = matchScreen.getChild<MatchSummaryPanel<*>>()
        val winnerScorer = summaryPanel.getChild<MatchScorer> { it.playerName == "Winner" }
        winnerScorer.lblResult.text shouldBe "2"

        val loserScorer = summaryPanel.getChild<MatchScorer> { it.playerName == "Loser" }
        loserScorer.lblResult.text shouldBe "0"

        // Select the first game
        val tabbedPane = matchScreen.getChild<JTabbedPane>()
        tabbedPane.selectedIndex = 1
        matchScreen.title shouldBe "Game #1 (501 - 2 players)"
    }
}