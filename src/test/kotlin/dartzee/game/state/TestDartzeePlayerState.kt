package dartzee.game.state

import dartzee.achievements.AchievementType
import dartzee.dartzee.DartzeeRoundResult
import dartzee.db.DartzeeRoundResultEntity
import dartzee.helper.AbstractTest
import dartzee.helper.getAchievementCount
import dartzee.helper.insertAchievement
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartzeePlayerState
import dartzee.helper.randomGuid
import dartzee.helper.retrieveAchievement
import dartzee.`object`.Dart
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class TestDartzeePlayerState : AbstractTest() {
    @Test
    fun `Should report a score of 0 when no darts have been thrown`() {
        val state = makeDartzeePlayerState(completedRounds = emptyList())
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `Should correctly calculate the cumulative score for a given round`() {
        val scoringRound = listOf(Dart(20, 1), Dart(7, 3), Dart(19, 1))
        val resultTwo = DartzeeRoundResult(1, false, -30)
        val resultThree = DartzeeRoundResult(7, true, 75)
        val resultFour = DartzeeRoundResult(2, false, -52)
        val resultFive = DartzeeRoundResult(3, true, 50)
        val state =
            makeDartzeePlayerState(
                completedRounds =
                    listOf(scoringRound, emptyList(), emptyList(), emptyList(), emptyList()),
                roundResults = listOf(resultTwo, resultThree, resultFour, resultFive),
            )

        state.getCumulativeScore(1) shouldBe 60
        state.getCumulativeScore(2) shouldBe 30
        state.getCumulativeScore(3) shouldBe 105
        state.getCumulativeScore(4) shouldBe 53
        state.getCumulativeScore(5) shouldBe 103
        state.getPeakScore() shouldBe 105
        state.getScoreSoFar() shouldBe 103
    }

    @Test
    fun `Should fire state changed`() {
        val state = makeDartzeePlayerState()
        state.shouldFireStateChange { it.addRoundResult(DartzeeRoundResultEntity()) }
        state.shouldFireStateChange { it.saveRoundResult(DartzeeRoundResult(1, true, 100)) }
    }

    @Test
    fun `Should save a round result`() {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(1, 1))
        val roundTwo = listOf(Dart(20, 1), Dart(20, 1), Dart(1, 1))
        val state = makeDartzeePlayerState(completedRounds = listOf(roundOne, roundTwo))

        state.saveRoundResult(DartzeeRoundResult(2, true, 50))

        val results = state.roundResults
        results.size shouldBe 1
        val result = results.first()
        result.ruleNumber shouldBe 2
        result.success shouldBe true
        result.score shouldBe 50
        result.roundNumber shouldBe 3
        result.retrievedFromDb shouldBe true
    }

    @Test
    fun `Should update Halved achievement`() {
        val p = insertPlayer()
        insertAchievement(
            playerId = p.rowId,
            type = AchievementType.DARTZEE_HALVED,
            gameIdEarned = randomGuid(),
            achievementCounter = 50,
        )

        val pt = insertParticipant(playerId = p.rowId)
        val state = makeDartzeePlayerState(pt)

        state.saveRoundResult(DartzeeRoundResult(2, false, -51))

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 51
        a.gameIdEarned shouldBe pt.gameId
    }

    @Test
    fun `Should not update Halved achievement if point loss is not higher than existing row`() {
        val p = insertPlayer()
        insertAchievement(
            playerId = p.rowId,
            type = AchievementType.DARTZEE_HALVED,
            gameIdEarned = randomGuid(),
            achievementCounter = 50,
        )

        val pt = insertParticipant(playerId = p.rowId)
        val state = makeDartzeePlayerState(pt)

        state.saveRoundResult(DartzeeRoundResult(2, false, -49))

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 50
        a.gameIdEarned shouldNotBe pt.gameId
    }

    @Test
    fun `Should not update Halved achievement if round was a success`() {
        val p = insertPlayer()
        val pt = insertParticipant(playerId = p.rowId)
        val state = makeDartzeePlayerState(pt)

        state.saveRoundResult(DartzeeRoundResult(2, true, 49))

        getAchievementCount(AchievementType.DARTZEE_HALVED) shouldBe 0
    }

    @Test
    fun `Should add loaded round results`() {
        val result = DartzeeRoundResult(2, true, 50)
        val entity = DartzeeRoundResultEntity.factoryAndSave(result, insertParticipant(), 2)

        val state = makeDartzeePlayerState()
        state.addRoundResult(entity)
        state.roundResults.shouldContainExactly(entity)
    }
}
