package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.ai.DartsAiModel
import dartzee.db.EntityName
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.X01PlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.preparePlayers
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.`object`.ComputedPoint
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.screen.game.x01.GameStatisticsPanelX01
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartsGamePanel : AbstractTest() {
    @Test
    fun `Should insert a win achievement for both players on a team`() {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = TestGamePanel()
        panel.startNewGame(listOf(team))

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

        panel.updateAchievementsForFinish(2, 50)

        getCountFromTable(EntityName.Achievement) shouldBe 0
    }

    @Test
    fun `Should unlock individual win achievement`() {
        val panel = TestGamePanel()
        val pt = makeSingleParticipant(insertPlayer(), panel.gameEntity.rowId)
        panel.startNewGame(listOf(pt))

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

        panel.updateAchievementsForFinish(3, 50)

        retrieveAchievementsForPlayer(pt.participant.playerId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_GAME, 50, panel.gameEntity.rowId),
            )
    }

    class TestGamePanel(private val config: X01Config = X01Config(501, FinishType.Doubles)) :
        GamePanelPausable<DartsScorerX01, X01PlayerState>(
            FakeDartsScreen(),
            insertGame(gameType = GameType.X01, gameParams = config.toJson()),
            1
        ) {
        override fun factoryState(pt: IWrappedParticipant) = X01PlayerState(501, pt)

        override fun computeAiDart(model: DartsAiModel): ComputedPoint? {
            val currentScore = getCurrentPlayerState().getRemainingScore()
            return model.throwX01Dart(currentScore)
        }

        override fun shouldStopAfterDartThrown() = getCurrentPlayerState().isCurrentRoundComplete()

        override fun currentPlayerHasFinished() = getCurrentPlayerState().getRemainingScore() == 0

        override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelX01(gameParams)

        override fun factoryScorer(participant: IWrappedParticipant) =
            DartsScorerX01(this, config.target, participant)
    }
}
