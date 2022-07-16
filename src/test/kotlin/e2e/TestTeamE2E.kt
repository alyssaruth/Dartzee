package e2e

import dartzee.ai.AimDart
import dartzee.game.GameType
import dartzee.game.prepareParticipants
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.insertGame
import dartzee.helper.predictableDartsModel
import dartzee.helper.retrieveTeam
import dartzee.`object`.Dart
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestTeamE2E : AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Team of 2`()
    {
        val game = insertGame(gameType = GameType.X01, gameParams = "501")

        val (gamePanel, listener) = setUpGamePanel(game)

        val p1Rounds = listOf(
            listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)), // 321
            listOf(Dart(20, 1), Dart(20, 3), Dart(5, 3)), // 179
            listOf(Dart(14, 1), Dart(20, 1), Dart(5, 1)), // 45
            listOf(Dart(3, 0), Dart(3, 1), Dart(16, 2)), // 19
            listOf(Dart(8, 0), Dart(8, 1), Dart(4, 1)), // 4
        )

        val p2Rounds = listOf(
            listOf(Dart(19, 1), Dart(3, 3), Dart(19, 1)), // 274
            listOf(Dart(19, 3), Dart(17, 1), Dart(7, 3)), // 84
            listOf(Dart(17, 1), Dart(14, 0), Dart(9, 1)), // 19
            listOf(Dart(3, 1)), // 16, mercied
            listOf(Dart(2, 0), Dart(2, 2)) // Fin.
        )

        val expectedRounds = p1Rounds.zip(p2Rounds) { p1Round, p2Round -> listOf(p1Round, p2Round) }.flatten()

        val p1AimDarts = p1Rounds.flatten().map { AimDart(it.score, it.multiplier) }
        val p2AimDarts = p2Rounds.flatten().map { AimDart(it.score, it.multiplier) }

        val p1Model = predictableDartsModel(gamePanel.dartboard, p1AimDarts, mercyThreshold = 7)
        val p2Model = predictableDartsModel(gamePanel.dartboard, p2AimDarts, mercyThreshold = 20)

        val p1 = makePlayerWithModel(p1Model, name = "Alan")
        val p2 = makePlayerWithModel(p2Model, name = "Lynn", image = "BaboTwo")

        val participants = prepareParticipants(game.rowId, listOf(p1, p2), true)
        gamePanel.startNewGame(participants)
        awaitGameFinish(game)

        verifyState(gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 29, pt = retrieveTeam())

//        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(
//            AchievementSummary(AchievementType.X01_BEST_FINISH, 4, game.rowId),
//            AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
//            AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 2, game.rowId),
//            AchievementSummary(AchievementType.X01_HIGHEST_BUST, 20, game.rowId),
//            AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 1, game.rowId)
//        )
//
//        checkAchievementConversions(player.rowId)
    }
}