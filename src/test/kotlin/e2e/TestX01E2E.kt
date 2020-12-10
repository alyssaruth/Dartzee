package e2e

import dartzee.`object`.Dart
import dartzee.achievements.*
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
import org.junit.jupiter.api.Test

class TestX01E2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    fun `E2E - 501 - 9 dart game`()
    {
        val game = insertGame(gameType = GameType.X01, gameParams = "501")

        val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = aiModel)

        val (panel, listener) = setUpGamePanel(game)

        panel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedRounds = listOf(
            listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
            listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
            listOf(Dart(20, 3), Dart(19, 3), Dart(12, 2))
        )

        verifyState(panel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 9)

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_GAME, 9, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_FINISH, 141, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
                AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 12, game.rowId)
        )
    }

    @Test
    fun `E2E - 301 - bust and mercy rule`()
    {
        val game = insertGame(gameType = GameType.X01, gameParams = "301")

        val parentWindow = DartsGameScreen(game, 1)
        parentWindow.isVisible = true

        val listener = mockk<DartboardListener>(relaxed = true)
        parentWindow.gamePanel.dartboard.addDartboardListener(listener)

        val expectedRounds = listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)), //121
                listOf(Dart(20, 3), Dart(20, 2), Dart(1, 1)),  // 20
                listOf(Dart(15, 2)),                           // 20 (bust)
                listOf(Dart(10, 1), Dart(5, 1), Dart(5, 0)),   //  5
                listOf(Dart(5, 0), Dart(5, 0), Dart(5, 0)),    //  5
                listOf(Dart(1, 1)),                            //  4 (mercy)
                listOf(Dart(2, 2))                             //  0
        )

        val aimDarts = expectedRounds.flatten().map { AimDart(it.score, it.multiplier) }
        val aiModel = predictableDartsModel(parentWindow.gamePanel.dartboard, aimDarts, mercyThreshold = 7)

        val player = makePlayerWithModel(aiModel)
        parentWindow.gamePanel.startNewGame(listOf(player))
        awaitGameFinish(game)

        verifyState(parentWindow.gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 19)

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_FINISH, 4, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
                AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 2, game.rowId),
                AchievementSummary(AchievementType.X01_HIGHEST_BUST, 20, game.rowId),
                AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 1, game.rowId)
        )
    }
}