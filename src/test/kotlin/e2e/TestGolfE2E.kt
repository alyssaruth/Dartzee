package e2e

import dartzee.*
import dartzee.`object`.Dart
import dartzee.achievements.AchievementType
import dartzee.ai.AimDart
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.listener.DartboardListener
import dartzee.screen.game.DartsGameScreen
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestGolfE2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Golf`()
    {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "18")

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val (panel, listener) = setUpGamePanel(game)
        panel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedDarts = (1..18).map { listOf(Dart(it, 2)) }
        verifyState(panel, listener, expectedDarts, finalScore = 18, expectedScorerRows = 20)

        val expectedAchievementRows = (1..18).map { AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "$it") } +
                AchievementSummary(AchievementType.GOLF_BEST_GAME, 18, game.rowId)

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(expectedAchievementRows)
        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Golf - Gambler, stop threshold`()
    {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "9")

        val parentWindow = DartsGameScreen(game, 1)
        parentWindow.isVisible = true

        val listener = mockk<DartboardListener>(relaxed = true)
        parentWindow.gamePanel.dartboard.addDartboardListener(listener)

        val expectedRounds = listOf(
            listOf(drtOuterOne(), drtInnerOne()), // 3, 1 gambled
            listOf(drtOuterFifteen(), drtTrebleSeventeen(), drtOuterSeventeen()), // 8, 1 gambled
            listOf(drtInnerThree(), drtOuterThree(), drtDoubleThree()), // 9, 4 gambled
            listOf(drtTrebleFour()), // 11, 4 gambled (tests first stopThreshold)
            listOf(drtDoubleFive()), // 12, 4 gambled
            listOf(drtOuterSix(), drtOuterSix(), drtOuterSix()), // 16, 6 gambled
            listOf(drtOuterSeven(), drtOuterSixteen(), drtInnerSixteen()), // 21, 7 gambled
            listOf(drtMissEight(), drtInnerEight()), // 24, 7 gambled
            listOf(drtMissNine(), drtDoubleNine()), // 25, 7 gambled
        )

        val aimDarts = expectedRounds.flatten().map { AimDart(it.score, it.multiplier, it.segmentType) }
        val aiModel = predictableDartsModel(parentWindow.gamePanel.dartboard, aimDarts)

        val player = makePlayerWithModel(aiModel)
        parentWindow.gamePanel.startNewGame(listOf(player))
        awaitGameFinish(game)

        verifyState(parentWindow.gamePanel, listener, expectedRounds, finalScore = 25, expectedScorerRows = 10)

        val expectedAchievementRows = listOf(
            AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "3"),
            AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "5"),
            AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "9"),
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, game.rowId, "1"),
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 3, game.rowId, "3"),
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, game.rowId, "6"),
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, game.rowId, "7"),
        )

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(expectedAchievementRows)
        checkAchievementConversions(player.rowId)
    }
}