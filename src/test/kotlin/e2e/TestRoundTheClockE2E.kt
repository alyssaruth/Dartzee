package e2e

import dartzee.`object`.Dart
import dartzee.achievements.AchievementType
import dartzee.ai.AimDart
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
import dartzee.helper.*
import dartzee.listener.DartboardListener
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.makeSingleParticipant
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestRoundTheClockE2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("e2e")
    fun `E2E - RTC - perfect game`()
    {
        val game = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson())

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val (panel, listener) = setUpGamePanel(game)
        panel.startNewGame(listOf(makeSingleParticipant(player)))
        awaitGameFinish(game)

        val expectedDarts = (1..20).map { Dart(it, 1) }.chunked(4)
        verifyState(panel, listener, expectedDarts, 20, scoreSuffix = " Darts")

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.CLOCK_BEST_GAME, 20, game.rowId),
                AchievementSummary(AchievementType.CLOCK_BEST_STREAK, 20, game.rowId),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "1"),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "2"),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "3"),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "4"),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "5")
        )

        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - RTC - unordered`()
    {
        val game = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Standard, false).toJson())

        val parentWindow = DartsGameScreen(game, 1)
        parentWindow.isVisible = true

        val listener = mockk<DartboardListener>(relaxed = true)
        parentWindow.gamePanel.dartboard.addDartboardListener(listener)

        val expectedRounds = listOf(
                listOf(Dart(1, 1), Dart(5, 3), Dart(20, 1)),               // 2,3,4,6,7,8,9,10,11,12,13,14,15,16,17,18,19
                listOf(Dart(3, 2), Dart(4, 0), Dart(7, 1)),                // 2,4,6,8,9,10,11,12,13,14,15,16,17,18,19
                listOf(Dart(8, 1), Dart(5, 1), Dart(20, 1)),               // 2,4,6,9,10,11,12,13,14,15,16,17,18,19
                listOf(Dart(2, 1), Dart(4, 1), Dart(6, 1), Dart(9, 1)),    // 10,11,12,13,14,15,16,17,18,19 (streak of 4)
                listOf(Dart(10, 1), Dart(12, 1), Dart(13, 2)),             // 11,14,15,16,17,18,19 (broken streak of 5)
                listOf(Dart(14, 1), Dart(16, 1), Dart(15, 2)),             // 11,17,18,19
                listOf(Dart(11, 1), Dart(17, 1), Dart(18, 1), Dart(3, 0)), // 19
                listOf(Dart(19, 1))                                        // done.
        )

        val aimDarts = expectedRounds.flatten().map { AimDart(it.score, it.multiplier) }
        val aiModel = predictableDartsModel(parentWindow.gamePanel.dartboard, aimDarts, mercyThreshold = 7)

        val player = makePlayerWithModel(aiModel)
        parentWindow.gamePanel.startNewGame(listOf(makeSingleParticipant(player)))
        awaitGameFinish(game)

        verifyState(parentWindow.gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 24)

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.CLOCK_BEST_STREAK, 5, game.rowId),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "4")
        )

        checkAchievementConversions(player.rowId)
    }
}