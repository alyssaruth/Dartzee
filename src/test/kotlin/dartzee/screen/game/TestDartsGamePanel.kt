package dartzee.screen.game

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.purgeWindows
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.achievements.AchievementType
import dartzee.achievements.x01.AchievementX01BestThreeDarts
import dartzee.ai.DartsAiModel
import dartzee.bean.DartLabel
import dartzee.db.EntityName
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.game.prepareParticipants
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.X01PlayerState
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.preparePlayers
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.`object`.ComputedPoint
import dartzee.`object`.Dart
import dartzee.only
import dartzee.screen.game.scorer.AchievementOverlay
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.screen.game.x01.GameStatisticsPanelX01
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import javax.swing.JButton
import org.junit.jupiter.api.Test

class TestDartsGamePanel : AbstractTest() {
    @Test
    fun `should show achievement popup on the correct scorer`() {
        val panel = TestGamePanel(totalPlayers = 2)
        val g = panel.gameEntity
        val (p1, p2) = preparePlayers(2)
        val (pt1, pt2) = prepareParticipants(g.rowId, listOf(p1, p2), false)
        panel.startNewGame(listOf(pt1, pt2))

        val p1Scorer = panel.getChild<DartsScorerX01> { it.participant == pt1 }
        val p2Scorer = panel.getChild<DartsScorerX01> { it.participant == pt2 }

        val achievement =
            AchievementX01BestThreeDarts().apply {
                attainedValue = 80
                gameIdEarned = g.rowId
                localGameIdEarned = g.localId
            }

        panel.achievementUnlocked(p1.rowId, achievement)
        p1Scorer.findChild<AchievementOverlay>().shouldNotBeNull()
        p2Scorer.findChild<AchievementOverlay>().shouldBeNull()
    }

    @Test
    fun `should not show achievement popup in party mode`() {
        InjectedThings.partyMode = true

        val panel = TestGamePanel(totalPlayers = 2)
        val g = panel.gameEntity
        val (p1, p2) = preparePlayers(2)
        val (pt1, pt2) = prepareParticipants(g.rowId, listOf(p1, p2), false)
        panel.startNewGame(listOf(pt1, pt2))

        val achievement =
            AchievementX01BestThreeDarts().apply {
                attainedValue = 80
                gameIdEarned = g.rowId
                localGameIdEarned = g.localId
            }

        panel.achievementUnlocked(p1.rowId, achievement)
        panel.findChild<AchievementOverlay>().shouldBeNull()
    }

    @Test
    fun `Should insert a win achievement for both players on a team`() {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = TestGamePanel()
        panel.startNewGame(listOf(team))
        panel.addCompletedRound(Dart(20, 1))

        panel.updateAchievementsForFinish(1, 50)

        retrieveAchievementsForPlayer(p1.rowId)
            .shouldContainExactly(
                AchievementSummary(
                    AchievementType.X01_TEAM_GAMES_WON,
                    -1,
                    panel.gameEntity.rowId,
                    "50"
                ),
            )

        retrieveAchievementsForPlayer(p2.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(
                    AchievementType.X01_TEAM_GAMES_WON,
                    -1,
                    panel.gameEntity.rowId,
                    "50"
                ),
            )
    }

    @Test
    fun `Should not unlock team win achievement if game was not won`() {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = TestGamePanel()
        panel.startNewGame(listOf(team))
        panel.addCompletedRound(Dart(20, 1))

        panel.updateAchievementsForFinish(2, 50)

        getCountFromTable(EntityName.Achievement) shouldBe 0
    }

    @Test
    fun `Should unlock individual win achievement`() {
        val panel = TestGamePanel()
        val pt = makeSingleParticipant(insertPlayer(), panel.gameEntity.rowId)
        panel.startNewGame(listOf(pt))
        panel.addCompletedRound(Dart(20, 1))

        panel.updateAchievementsForFinish(1, 50)

        retrieveAchievementsForPlayer(pt.participant.playerId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_GAMES_WON, -1, panel.gameEntity.rowId, "50"),
                AchievementSummary(AchievementType.X01_BEST_GAME, 50, panel.gameEntity.rowId),
            )
    }

    @Test
    fun `Should not unlock individual win achievement if they did not place first`() {
        val panel = TestGamePanel()
        val pt = makeSingleParticipant(insertPlayer(), panel.gameEntity.rowId)
        panel.startNewGame(listOf(pt))
        panel.addCompletedRound(Dart(20, 1))

        panel.updateAchievementsForFinish(3, 50)

        retrieveAchievementsForPlayer(pt.participant.playerId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_GAME, 50, panel.gameEntity.rowId),
            )
    }

    @Test
    fun `Should toggle button visibility as a game progresses`() {
        val panel = TestGamePanel(totalPlayers = 2)

        val human = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)
        val ai =
            makeSingleParticipant(
                insertPlayer(strategy = DartsAiModel.new().toJson()),
                panel.gameEntity.rowId
            )

        panel.startNewGame(listOf(human, ai))
        panel.resignButton().shouldBeVisible()
        panel.confirmButton().shouldNotBeVisible()
        panel.resetButton().shouldNotBeVisible()

        panel.dartThrown(Dart(20, 1))
        panel.confirmButton().shouldBeVisible()
        panel.resetButton().shouldBeVisible()

        panel.confirmButton().doClick()

        // AI Turn
        panel.resignButton().shouldNotBeVisible()
        panel.confirmButton().shouldNotBeVisible()
        panel.resetButton().shouldNotBeVisible()

        panel.dartThrown(Dart(20, 1))
        panel.resignButton().shouldNotBeVisible()
        panel.confirmButton().shouldNotBeVisible()
        panel.resetButton().shouldNotBeVisible()
    }

    @Test
    fun `Should not show resign button in a practice game`() {
        val panel = TestGamePanel(totalPlayers = 1)

        val player = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)

        panel.startNewGame(listOf(player))
        panel.resignButton().shouldNotBeVisible()
    }

    @Test
    fun `Should not show resign button if only one active player remaining`() {
        val panel = TestGamePanel(totalPlayers = 3)

        val human1 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)
        val human2 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)
        val human3 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)

        panel.startNewGame(listOf(human1, human2, human3))
        panel.resignButton().shouldBeVisible()
        panel.clickResign()
        getQuestionDialog().clickYes(async = true)
        purgeWindows()

        panel.resignButton().shouldBeVisible()
        panel.clickResign()
        getQuestionDialog().clickYes(async = true)

        panel.resignButton().shouldNotBeVisible()
    }

    @Test
    fun `Should not resign a player if cancelled`() {
        val panel = TestGamePanel(totalPlayers = 3)

        val human1 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)
        val human2 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)

        panel.startNewGame(listOf(human1, human2))
        panel.resignButton().shouldBeVisible()
        panel.clickResign()
        getQuestionDialog().clickNo(async = true)

        panel.getPlayerStates().count { it.hasResigned() } shouldBe 0
    }

    @Test
    fun `Should resign a player and clear the dartboard`() {
        val panel = TestGamePanel(totalPlayers = 3)

        val human1 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)
        val human2 = makeSingleParticipant(insertPlayer(strategy = ""), panel.gameEntity.rowId)

        panel.startNewGame(listOf(human1, human2))
        panel.dartThrown(Dart(20, 1))
        panel.resignButton().shouldBeVisible()
        panel.clickResign()
        getQuestionDialog().clickYes(async = true)

        val human1State = panel.getPlayerStates().filter { it.wrappedParticipant == human1 }.only()
        human1State.hasResigned() shouldBe true
        panel.dartboard.findChild<DartLabel>() shouldBe null
    }

    private fun TestGamePanel.clickResign() =
        clickChild<JButton>(async = true) { it.toolTipText == "Resign" }

    private fun TestGamePanel.confirmButton() =
        getChild<JButton> { it.toolTipText == "Confirm round" }

    private fun TestGamePanel.resetButton() = getChild<JButton> { it.toolTipText == "Reset round" }

    private fun TestGamePanel.resignButton() = getChild<JButton> { it.toolTipText == "Resign" }

    class TestGamePanel(
        private val config: X01Config = X01Config(501, FinishType.Doubles),
        totalPlayers: Int = 1
    ) :
        GamePanelPausable<DartsScorerX01, X01PlayerState>(
            FakeDartsScreen(),
            insertGame(gameType = GameType.X01, gameParams = config.toJson()),
            totalPlayers
        ) {
        override fun factoryState(pt: IWrappedParticipant) = X01PlayerState(config, pt)

        override fun computeAiDart(model: DartsAiModel): ComputedPoint {
            val currentScore = getCurrentPlayerState().getRemainingScore()
            return model.throwX01Dart(currentScore, config.finishType, 3 - dartsThrownCount())
        }

        override fun shouldStopAfterDartThrown() = getCurrentPlayerState().isCurrentRoundComplete()

        override fun currentPlayerHasFinished() = getCurrentPlayerState().getRemainingScore() == 0

        override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelX01(gameParams)

        override fun factoryScorer(participant: IWrappedParticipant) =
            DartsScorerX01(this, config.target, participant)
    }
}
