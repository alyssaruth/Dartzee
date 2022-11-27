package e2e

import dartzee.achievements.AchievementType
import dartzee.ai.AimDart
import dartzee.drtDoubleEleven
import dartzee.drtInnerEight
import dartzee.drtInnerFourteen
import dartzee.drtInnerSeven
import dartzee.drtInnerSeventeen
import dartzee.drtInnerTen
import dartzee.drtInnerThree
import dartzee.drtMissThree
import dartzee.drtMissTwelve
import dartzee.drtOuterEighteen
import dartzee.drtOuterEleven
import dartzee.drtOuterFifteen
import dartzee.drtOuterFive
import dartzee.drtOuterFour
import dartzee.drtOuterNine
import dartzee.drtOuterOne
import dartzee.drtOuterSeven
import dartzee.drtOuterSeventeen
import dartzee.drtOuterSixteen
import dartzee.drtOuterThree
import dartzee.drtOuterTwelve
import dartzee.drtOuterTwenty
import dartzee.drtOuterTwo
import dartzee.drtTrebleNineteen
import dartzee.drtTrebleSix
import dartzee.drtTrebleThirteen
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

        val (panel, listener) = setUpGamePanelAndStartGame(game, listOf(player))
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

        val (gamePanel, listener) = setUpGamePanel(game)

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
        val aiModel = predictableDartsModel(aimDarts, mercyThreshold = 7)

        val player = makePlayerWithModel(aiModel)
        gamePanel.startGame(listOf(player))
        awaitGameFinish(game)

        verifyState(gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 24)

        retrieveAchievementsForPlayer(player.rowId).shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.CLOCK_BEST_STREAK, 5, game.rowId),
                AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "4")
        )

        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("e2e")
    fun `E2E - In Order- Team of 2`()
    {
        val game = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson())
        val (gamePanel, listener) = setUpGamePanel(game)

        val p1Rounds = listOf(
            listOf(drtOuterOne(), drtOuterTwo(), drtMissThree()), // Target: 3
            listOf(drtOuterFive(), drtTrebleSix(), drtOuterSeven(), drtInnerEight()), // Target: 9
            listOf(drtOuterTwelve(), drtTrebleThirteen(), drtOuterEleven()), // Target: 14
            listOf(drtOuterSeventeen(), drtOuterEighteen(), drtInnerSeven()), // Target: 19
            listOf(drtOuterTwenty()) // Fin
        )

        val p2Rounds = listOf(
            listOf(drtInnerSeventeen(), drtInnerThree(), drtOuterFour()), // Target: 5
            listOf(drtOuterNine(), drtInnerTen(), drtDoubleEleven(), drtMissTwelve()), // Target: 12
            listOf(drtInnerFourteen(), drtOuterFifteen(), drtOuterSixteen(), drtOuterThree()), // Target: 17
            listOf(drtTrebleNineteen(), drtOuterFive(), drtOuterOne()) // Target: 20
        )

        val expectedRounds: List<List<Dart>> = p1Rounds.zipDartRounds(p2Rounds)

        val p1AimDarts = p1Rounds.flatten().map { it.toAimDart() }
        val p2AimDarts = p2Rounds.flatten().map { it.toAimDart() }

        val p1Model = predictableDartsModel(p1AimDarts)
        val p2Model = predictableDartsModel(p2AimDarts)

        val p1 = makePlayerWithModel(p1Model, name = "Alan")
        val p2 = makePlayerWithModel(p2Model, name = "Lynn", image = "BaboTwo")

        val participants = prepareParticipants(game.rowId, listOf(p1, p2), true)
        gamePanel.startNewGame(participants)
        awaitGameFinish(game)

        verifyState(gamePanel, listener, expectedRounds, finalScore = 28, pt = retrieveTeam(), scoreSuffix = " Darts")

        retrieveAchievementsForPlayer(p1.rowId).shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, game.rowId, "3"),
        )

        retrieveAchievementsForPlayer(p2.rowId).shouldBeEmpty()

        checkAchievementConversions(listOf(p1.rowId, p2.rowId))
    }
}