package dartzee.e2e

import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.achievements.AchievementType
import dartzee.ai.AimDart
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.game.prepareParticipants
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.AchievementSummary
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.beastDartsModel
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.makeDart
import dartzee.helper.predictableDartsModel
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.helper.retrieveTeam
import dartzee.`object`.Dart
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import dartzee.zipDartRounds
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestX01E2E : AbstractRegistryTest() {
    override fun getPreferencesAffected() =
        listOf(PREFERENCES_INT_AI_SPEED, PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE)

    @BeforeEach
    fun beforeEach() {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, true)
    }

    @Test
    @Tag("e2e")
    fun `E2E - 501 - 9 dart game`() {
        val game = insertGame(gameType = GameType.X01, gameParams = DEFAULT_X01_CONFIG.toJson())

        val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = aiModel)

        val (panel, listener) = setUpGamePanelAndStartGame(game, listOf(player))
        awaitGameFinish(game)

        val expectedRounds =
            listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(19, 3), Dart(12, 2))
            )

        verifyState(panel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 9)

        retrieveAchievementsForPlayer(player.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_GAME, 9, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_FINISH, 141, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
                AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 12, game.rowId),
                AchievementSummary(
                    AchievementType.X01_STYLISH_FINISH,
                    141,
                    game.rowId,
                    "T20, T19, D12"
                )
            )

        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - 501 - 9 dart game, relaxed finish`() {
        val game =
            insertGame(
                gameType = GameType.X01,
                gameParams = X01Config(501, FinishType.Any).toJson()
            )

        val player = insertPlayer(model = beastDartsModel())

        val (panel, listener) = setUpGamePanelAndStartGame(game, listOf(player))
        awaitGameFinish(game)

        val expectedRounds =
            listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(20, 3), Dart(7, 3))
            )

        verifyState(panel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 9)

        retrieveAchievementsForPlayer(player.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
            )

        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - 101 - relaxed`() {
        val game =
            insertGame(
                gameType = GameType.X01,
                gameParams = X01Config(101, FinishType.Any).toJson()
            )

        val (gamePanel) = setUpGamePanel(game, 2)

        val p1Rounds =
            listOf(
                listOf(makeDart(20, 3), makeDart(20, 1), makeDart(20, 1)), // 1
                listOf(makeDart(20, 1)), // 1 (bust)
                listOf(makeDart(1, 0), makeDart(1, 0), makeDart(1, 1)), // 0
            )

        val p2Rounds =
            listOf(
                listOf(makeDart(20, 1), makeDart(5, 1), makeDart(1, 1)), // 75
                listOf(makeDart(20, 3), makeDart(5, 1), makeDart(20, 2)), // 75 (bust)
                listOf(makeDart(20, 3), makeDart(5, 1), makeDart(5, 2)) // 0
            )

        val p1Model = predictableDartsModel(p1Rounds.flatten().map { it.toAimDart() })
        val p1 = makePlayerWithModel(p1Model)

        val p2Model = predictableDartsModel(p2Rounds.flatten().map { it.toAimDart() })
        val p2 = makePlayerWithModel(p2Model, "Jeff")

        val (pt1, pt2) = gamePanel.startGame(listOf(p1, p2))
        awaitGameFinish(game)
        waitForAssertion { pt2.participant.finalScore shouldBeGreaterThan -1 }

        pt1.participant.finalScore shouldBe 9
        pt1.participant.finishingPosition shouldBe 1
        pt2.participant.finalScore shouldBe 9
        pt2.participant.finishingPosition shouldBe 2

        retrieveAchievementsForPlayer(p1.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 100, game.rowId),
                AchievementSummary(AchievementType.X01_HIGHEST_BUST, 1, game.rowId),
                AchievementSummary(AchievementType.X01_GAMES_WON, -1, game.rowId, "9"),
            )

        retrieveAchievementsForPlayer(p2.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_HOTEL_INSPECTOR, -1, game.rowId, "20, 5, 1"),
                AchievementSummary(AchievementType.X01_BEST_FINISH, 75, game.rowId),
                AchievementSummary(
                    AchievementType.X01_STYLISH_FINISH,
                    75,
                    game.rowId,
                    "T20, 5, D5"
                ),
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 75, game.rowId),
                AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 5, game.rowId),
                AchievementSummary(AchievementType.X01_HIGHEST_BUST, 75, game.rowId),
                AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 1, game.rowId)
            )

        checkAchievementConversions(p2.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - 301 - bust and mercy rule`() {
        val game =
            insertGame(
                gameType = GameType.X01,
                gameParams = X01Config(301, FinishType.Doubles).toJson()
            )

        val (gamePanel, listener) = setUpGamePanel(game)

        val expectedRounds =
            listOf(
                listOf(makeDart(20, 3), makeDart(20, 3), makeDart(20, 3)), // 121
                listOf(makeDart(20, 3), makeDart(20, 2), makeDart(1, 1)), // 20
                listOf(makeDart(15, 2)), // 20 (bust)
                listOf(makeDart(10, 1), makeDart(5, 1), makeDart(5, 0)), //  5
                listOf(makeDart(5, 0), makeDart(5, 0), makeDart(5, 0)), //  5
                listOf(makeDart(1, 1)), //  4 (mercy)
                listOf(makeDart(2, 2)) //  0
            )

        val aimDarts = expectedRounds.flatten().map { it.toAimDart() }
        val aiModel = predictableDartsModel(aimDarts, mercyThreshold = 7)

        val player = makePlayerWithModel(aiModel)
        gamePanel.startGame(listOf(player))
        awaitGameFinish(game)

        verifyState(gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 19)

        retrieveAchievementsForPlayer(player.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_FINISH, 4, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
                AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 2, game.rowId),
                AchievementSummary(AchievementType.X01_HIGHEST_BUST, 20, game.rowId),
                AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 1, game.rowId)
            )

        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - 501 - Team of 2`() {
        val game = insertGame(gameType = GameType.X01, gameParams = DEFAULT_X01_CONFIG.toJson())
        val (gamePanel, listener) = setUpGamePanel(game)

        val p1Rounds =
            listOf(
                listOf(makeDart(20, 3), makeDart(20, 3), makeDart(20, 3)), // 321
                listOf(makeDart(20, 1), makeDart(20, 3), makeDart(5, 3)), // 179
                listOf(makeDart(14, 1), makeDart(20, 1), makeDart(5, 1)), // 45
                listOf(makeDart(3, 0), makeDart(3, 1), makeDart(16, 2)), // 19
                listOf(makeDart(8, 0), makeDart(8, 0), makeDart(16, 0)), // 16
                listOf(makeDart(1, 1), makeDart(4, 0), makeDart(4, 2)), // Fin
            )

        val p2Rounds =
            listOf(
                listOf(makeDart(19, 1), makeDart(3, 3), makeDart(19, 1)), // 274
                listOf(makeDart(19, 3), makeDart(17, 1), makeDart(7, 3)), // 84
                listOf(makeDart(17, 1), makeDart(14, 0), makeDart(9, 1)), // 19
                listOf(makeDart(3, 1)), // 16, mercied
                listOf(makeDart(8, 0), makeDart(8, 0), makeDart(7, 1)) // 9
            )

        val expectedRounds = p1Rounds.zipDartRounds(p2Rounds)

        val p1AimDarts = p1Rounds.flatten().map { it.toAimDart() }
        val p2AimDarts = p2Rounds.flatten().map { it.toAimDart() }

        val p1Model = predictableDartsModel(p1AimDarts, mercyThreshold = 7)
        val p2Model = predictableDartsModel(p2AimDarts, mercyThreshold = 20)

        val p1 = makePlayerWithModel(p1Model, name = "Alan")
        val p2 = makePlayerWithModel(p2Model, name = "Lynn", image = "BaboTwo")

        val participants = prepareParticipants(game.rowId, listOf(p1, p2), true)
        gamePanel.startNewGame(participants)
        awaitGameFinish(game)

        verifyState(
            gamePanel,
            listener,
            expectedRounds,
            scoreSuffix = " Darts",
            finalScore = 33,
            pt = retrieveTeam()
        )

        retrieveAchievementsForPlayer(p1.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 180, game.rowId),
                AchievementSummary(AchievementType.X01_HIGHEST_BUST, 19, game.rowId),
                AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 1, game.rowId),
                AchievementSummary(AchievementType.X01_NO_MERCY, -1, game.rowId, "9"),
                AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 4, game.rowId),
                AchievementSummary(AchievementType.X01_BEST_FINISH, 9, game.rowId),
            )

        retrieveAchievementsForPlayer(p2.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 95, game.rowId),
            )

        checkAchievementConversions(listOf(p1.rowId, p2.rowId))
    }
}
