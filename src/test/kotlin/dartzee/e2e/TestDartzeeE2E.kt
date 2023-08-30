package dartzee.e2e

import dartzee.achievements.AchievementType
import dartzee.ai.DartzeePlayStyle
import dartzee.dartzee.DartzeeCalculator
import dartzee.db.DartzeeRoundResultEntity
import dartzee.game.GameType
import dartzee.game.prepareParticipants
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.AchievementSummary
import dartzee.helper.allTwenties
import dartzee.helper.beastDartsModel
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.makeDart
import dartzee.helper.predictableDartsModel
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.helper.retrieveParticipant
import dartzee.helper.retrieveTeam
import dartzee.helper.scoreEighteens
import dartzee.helper.testRules
import dartzee.helper.totalIsFifty
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.utils.InjectedThings
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import dartzee.utils.insertDartzeeRules
import dartzee.zipDartRounds
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestDartzeeE2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("dartzee/e2e")
    fun `E2E - Dartzee`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val rules = listOf(scoreEighteens, allTwenties)
        insertDartzeeRules(game.rowId, rules)

        val (panel, listener) = setUpGamePanelAndStartGame(game, listOf(player))
        awaitGameFinish(game)

        val expectedRounds = listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)), //Scoring round
                listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1)), //All Twenties
                listOf(Dart(18, 1), Dart(18, 1), Dart(25, 2)) //Score Eighteens
        )

        verifyState(panel, listener, expectedRounds, finalScore = 276)

        val participantId = retrieveParticipant().rowId

        val results = DartzeeRoundResultEntity().retrieveEntities().sortedBy { it.roundNumber }
        val roundOne = results.first()
        roundOne.success shouldBe true
        roundOne.ruleNumber shouldBe 2
        roundOne.score shouldBe 60
        roundOne.participantId shouldBe participantId

        val roundTwo = results[1]
        roundTwo.success shouldBe true
        roundTwo.ruleNumber shouldBe 1
        roundTwo.score shouldBe 36
        roundTwo.participantId shouldBe participantId

        checkAchievementConversions(player.rowId)
    }

    @Test
    @Tag("dartzee/e2e")
    fun `E2E - Dartzee - 2 player team`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)
        insertDartzeeRules(game.rowId, testRules)

        val (gamePanel, listener) = setUpGamePanel(game)

        val p1Rounds = listOf(
            listOf(makeDart(20, 1), makeDart(5, 1), makeDart(1, 1)), // Scoring round - 26
            listOf(makeDart(18, 1), makeDart(19, 1), makeDart(18, 1)), // Score 18s & 2B1W, picks 2B1W - 107
            listOf(makeDart(20, 1), makeDart(18, 1), makeDart(12, 1)), // Total is 50 - 175
        )

        val p2Rounds = listOf(
            listOf(
                makeDart(20, 1, SegmentType.INNER_SINGLE),
                makeDart(5, 1, SegmentType.OUTER_SINGLE),
                makeDart(1, 1, SegmentType.INNER_SINGLE)), // IOI - 52
            listOf(makeDart(18, 1), makeDart(20, 1), makeDart(5, 1)), // Score 18s - 125
        )

        val expectedRounds = p1Rounds.zipDartRounds(p2Rounds)

        val p1AimDarts = p1Rounds.flatten().map { it.toAimDart() }
        val p2AimDarts = p2Rounds.flatten().map { it.toAimDart() }

        val p1Model = predictableDartsModel(p1AimDarts, mercyThreshold = 7, dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE)
        val p2Model = predictableDartsModel(p2AimDarts, mercyThreshold = 20)

        val p1 = makePlayerWithModel(p1Model, name = "Alan")
        val p2 = makePlayerWithModel(p2Model, name = "Lynn", image = "BaboTwo")

        val participants = prepareParticipants(game.rowId, listOf(p1, p2), true)
        gamePanel.startNewGame(participants)

        awaitGameFinish(game)
        verifyState(gamePanel, listener, expectedRounds, finalScore = 175, pt = retrieveTeam())

        retrieveAchievementsForPlayer(p1.rowId).shouldContainExactly(
            AchievementSummary(AchievementType.DARTZEE_UNDER_PRESSURE, 50, game.rowId, totalIsFifty.getDisplayName())
        )

        retrieveAchievementsForPlayer(p2.rowId).shouldBeEmpty()

        checkAchievementConversions(listOf(p1.rowId, p2.rowId))
    }
}