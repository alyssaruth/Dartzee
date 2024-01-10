package dartzee.e2e

import dartzee.achievements.AchievementType
import dartzee.drtDoubleEleven
import dartzee.drtDoubleFive
import dartzee.drtDoubleNine
import dartzee.drtDoubleSix
import dartzee.drtDoubleSixteen
import dartzee.drtDoubleThree
import dartzee.drtInnerEight
import dartzee.drtInnerFour
import dartzee.drtInnerOne
import dartzee.drtInnerSeven
import dartzee.drtInnerSixteen
import dartzee.drtInnerThree
import dartzee.drtMissEight
import dartzee.drtMissFour
import dartzee.drtMissNine
import dartzee.drtMissOne
import dartzee.drtMissSeven
import dartzee.drtMissThree
import dartzee.drtMissTwo
import dartzee.drtOuterFifteen
import dartzee.drtOuterFour
import dartzee.drtOuterNine
import dartzee.drtOuterOne
import dartzee.drtOuterSeven
import dartzee.drtOuterSeventeen
import dartzee.drtOuterSix
import dartzee.drtOuterSixteen
import dartzee.drtOuterThree
import dartzee.drtOuterTwo
import dartzee.drtTrebleFour
import dartzee.drtTrebleSeventeen
import dartzee.game.GameType
import dartzee.game.prepareParticipants
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.AchievementSummary
import dartzee.helper.beastDartsModel
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.predictableDartsModel
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.helper.retrieveTeam
import dartzee.`object`.Dart
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import dartzee.zipDartRounds
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestGolfE2E : AbstractRegistryTest() {
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach() {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Golf`() {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "18")

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val (panel, listener) = setUpGamePanelAndStartGame(game, listOf(player))
        awaitGameFinish(game)

        val expectedDarts = (1..18).map { listOf(Dart(it, 2)) }
        verifyState(panel, listener, expectedDarts, finalScore = 18, expectedScorerRows = 20)

        val expectedAchievementRows =
            (1..18).map {
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "$it")
            } +
                AchievementSummary(AchievementType.GOLF_BEST_GAME, 18, game.rowId) +
                AchievementSummary(AchievementType.GOLF_ONE_HIT_WONDER, 18, game.rowId) +
                AchievementSummary(AchievementType.GOLF_IN_BOUNDS, -1, game.rowId, "18")

        retrieveAchievementsForPlayer(player.rowId)
            .shouldContainExactlyInAnyOrder(expectedAchievementRows)
        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Golf - Gambler, stop threshold`() {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "9")
        val (gamePanel, listener) = setUpGamePanel(game)

        val expectedRounds =
            listOf(
                listOf(drtOuterOne(), drtInnerOne()), // 3, 1 gambled
                listOf(
                    drtOuterFifteen(),
                    drtTrebleSeventeen(),
                    drtOuterSeventeen()
                ), // 8, 1 gambled
                listOf(drtInnerThree(), drtOuterThree(), drtDoubleThree()), // 9, 4 gambled, OHW: 1
                listOf(drtTrebleFour()), // 11, 4 gambled (tests first stopThreshold)
                listOf(drtDoubleFive()), // 12, 4 gambled, OHW: 2
                listOf(drtOuterSix(), drtOuterSix(), drtOuterSix()), // 16, 6 gambled
                listOf(drtOuterSeven(), drtOuterSixteen(), drtInnerSixteen()), // 21, 7 gambled
                listOf(drtMissEight(), drtInnerEight()), // 24, 7 gambled
                listOf(drtMissNine(), drtDoubleNine()), // 25, 7 gambled, OHW: 3
            )

        val aimDarts = expectedRounds.flatten().map { it.toAimDart() }
        val aiModel = predictableDartsModel(aimDarts)

        val player = makePlayerWithModel(aiModel)
        gamePanel.startGame(listOf(player))
        awaitGameFinish(game)

        verifyState(gamePanel, listener, expectedRounds, finalScore = 25, expectedScorerRows = 10)

        val expectedAchievementRows =
            listOf(
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "3"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "5"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "9"),
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, game.rowId, "1"),
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 3, game.rowId, "3"),
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, game.rowId, "6"),
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, game.rowId, "7"),
                AchievementSummary(AchievementType.GOLF_ONE_HIT_WONDER, 3, game.rowId)
            )

        retrieveAchievementsForPlayer(player.rowId)
            .shouldContainExactlyInAnyOrder(expectedAchievementRows)
        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - 9 holes - Team of 2`() {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "9")
        val (gamePanel, listener) = setUpGamePanel(game)

        val p1Rounds =
            listOf(
                listOf(
                    drtMissOne(),
                    drtOuterOne(),
                    drtOuterOne()
                ), // Gambled 1 in round 1. Total: 4
                listOf(drtMissThree(), drtDoubleThree()), // CM: 3, OHW: 1, Total: 9
                listOf(drtDoubleFive()), // CM: 5, OHW: 2, Total: 13
                listOf(drtMissSeven(), drtInnerSeven()), // Total: 17
                listOf(
                    drtOuterNine(),
                    drtOuterNine(),
                    drtDoubleNine()
                ), // Gambled 2, CM: 9, OHW: 3, Total: 23
            )

        val p2Rounds =
            listOf(
                listOf(drtMissTwo(), drtOuterTwo()), // Total: 8
                listOf(
                    drtOuterFour(),
                    drtMissFour(),
                    drtInnerFour()
                ), // Gambled 1 in round 4, Total: 12
                listOf(drtDoubleSix()), // CM: 6, OHW: 1, Total: 14
                listOf(drtDoubleSixteen(), drtDoubleEleven(), drtMissEight()) // Total: 22
            )

        val expectedRounds: List<List<Dart>> = p1Rounds.zipDartRounds(p2Rounds)

        val p1AimDarts = p1Rounds.flatten().map { it.toAimDart() }
        val p2AimDarts = p2Rounds.flatten().map { it.toAimDart() }

        val p1Model = predictableDartsModel(p1AimDarts, golfStopThresholds = mapOf(1 to 1, 2 to 3))
        val p2Model = predictableDartsModel(p2AimDarts, golfStopThresholds = mapOf(1 to 3, 2 to 4))

        val p1 = makePlayerWithModel(p1Model, name = "Alan")
        val p2 = makePlayerWithModel(p2Model, name = "Lynn", image = "BaboTwo")

        val participants = prepareParticipants(game.rowId, listOf(p1, p2), true)
        gamePanel.startNewGame(participants)
        awaitGameFinish(game)

        verifyState(
            gamePanel,
            listener,
            expectedRounds,
            finalScore = 23,
            pt = retrieveTeam(),
            expectedScorerRows = 10
        )

        retrieveAchievementsForPlayer(p1.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, game.rowId, "1"),
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, game.rowId, "9"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "3"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "5"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "9"),
                AchievementSummary(AchievementType.GOLF_ONE_HIT_WONDER, 3, game.rowId)
            )

        retrieveAchievementsForPlayer(p2.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, game.rowId, "4"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, game.rowId, "6"),
                AchievementSummary(AchievementType.GOLF_ONE_HIT_WONDER, 1, game.rowId)
            )

        checkAchievementConversions(listOf(p1.rowId, p2.rowId))
    }
}
