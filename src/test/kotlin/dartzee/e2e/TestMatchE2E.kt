package dartzee.e2e

import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.core.util.DateStatics
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.FinishType
import dartzee.game.GameLaunchParams
import dartzee.game.GameLauncher
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.game.X01Config
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.insertDartsMatch
import dartzee.helper.retrieveDartsMatch
import dartzee.preferences.Preferences
import dartzee.screen.ScreenCache
import dartzee.screen.game.MatchSummaryPanel
import dartzee.screen.game.scorer.MatchScorer
import dartzee.screen.game.x01.X01MatchScreen
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import javax.swing.JTabbedPane
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestMatchE2E : AbstractE2ETest() {
    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        InjectedThings.preferenceService.save(Preferences.aiAutoContinue, false)
    }

    @Test
    fun `E2E - Two game match`() {
        val match = insertDartsMatch(games = 2, matchParams = "", mode = MatchMode.FIRST_TO)
        match.gameType = GameType.X01
        match.gameParams = "501"

        val (winner, loser) = createPlayers()
        val launchParams =
            GameLaunchParams(
                listOf(winner, loser),
                GameType.X01,
                DEFAULT_X01_CONFIG.toJson(),
                false
            )
        GameLauncher().launchNewMatch(match, launchParams)

        waitForAssertion { retrieveDartsMatch().dtFinish shouldNotBe DateStatics.END_OF_TIME }

        verifyDatabase(match.rowId, winner, loser)
        verifyUi()
    }

    private fun verifyDatabase(matchId: String, winner: PlayerEntity, loser: PlayerEntity) {
        val games = GameEntity().retrieveEntities()
        games.size shouldBe 2
        games.forEach {
            it.gameParams shouldBe X01Config(501, FinishType.Doubles).toJson()
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

    private fun verifyUi() {
        val matchScreen = ScreenCache.getDartsGameScreens().first() as X01MatchScreen
        matchScreen.title shouldBe "Match #1 (First to 2 - 501)"

        val summaryPanel = matchScreen.getChild<MatchSummaryPanel<*>>()
        val winnerScorer = summaryPanel.getChild<MatchScorer> { it.playerName.contains("Winner") }
        winnerScorer.lblResult.text shouldBe "2"

        val loserScorer = summaryPanel.getChild<MatchScorer> { it.playerName.contains("Loser") }
        loserScorer.lblResult.text shouldBe "0"

        // Select the first game
        val tabbedPane = matchScreen.getChild<JTabbedPane>()
        tabbedPane.selectedIndex = 1
        matchScreen.title shouldBe "Game #1 (501 - 2 players)"
    }
}
