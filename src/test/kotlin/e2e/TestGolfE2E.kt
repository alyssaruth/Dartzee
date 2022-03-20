package e2e

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
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

        val roundOne = listOf(makeDart(1, 1, SegmentType.OUTER_SINGLE), makeDart(1, 1, SegmentType.INNER_SINGLE))
        val roundTwo = listOf(makeDart(15, 1, SegmentType.OUTER_SINGLE), makeDart(17, 3), makeDart(17, 1))
        val roundThree = listOf(makeDart(3, 1, SegmentType.INNER_SINGLE), makeDart(3, 1, SegmentType.OUTER_SINGLE), makeDart(3, 2, SegmentType.DOUBLE))

        val expectedRounds = listOf(
            roundOne, // 3, 1 gambled
            roundTwo, // 8, 1 gambled
            roundThree // 9, 4 gambled
        )

        val aimDarts = expectedRounds.flatten().map { AimDart(it.score, it.multiplier, it.segmentType) }
        val aiModel = predictableDartsModel(parentWindow.gamePanel.dartboard, aimDarts)

        val player = makePlayerWithModel(aiModel)
        parentWindow.gamePanel.startNewGame(listOf(player))
        awaitGameFinish(game)
    }
}