package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.preparePlayers
import dartzee.helper.randomGuid
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.`object`.Dart
import dartzee.segmentStatuses
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGamePanelRoundTheClock: AbstractTest()
{
    @Test
    fun `Should not update the achievement for a completed hit streak of 1`()
    {
        val playerId = randomGuid()
        val panel = makeRoundTheClockGamePanel(playerId)

        val dartsThrown = listOf(factoryClockHit(1), Dart(15, 1), Dart(12, 2))
        panel.addCompletedRound(dartsThrown)

        AchievementEntity().retrieveEntity("PlayerId = '$playerId'") shouldBe null
    }

    @Test
    fun `Should not update the achievement for a partial hit streak of 1`()
    {
        val playerId = randomGuid()
        val panel = makeRoundTheClockGamePanel(playerId)

        val dartsThrown = listOf(Dart(15, 1), Dart(12, 2), factoryClockHit(1))
        panel.addCompletedRound(dartsThrown)

        AchievementEntity().retrieveEntity("PlayerId = '$playerId'") shouldBe null
    }

    @Test
    fun `Should save the best streak even when the player has subsequently missed`()
    {
        val playerId = randomGuid()
        val panel = makeRoundTheClockGamePanel(playerId)

        val dartsThrown = listOf(factoryClockHit(1), factoryClockHit(2), Dart(12, 2))
        panel.addCompletedRound(dartsThrown)

        val achievement = AchievementEntity().retrieveEntity("PlayerId = '$playerId'")!!
        achievement.achievementCounter shouldBe 2
        achievement.achievementType shouldBe AchievementType.CLOCK_BEST_STREAK
    }

    @Test
    fun `Should add on to the current streak if one exists`()
    {
        val playerId = randomGuid()
        val panel = makeRoundTheClockGamePanel(playerId)

        val dartsThrown = listOf(factoryClockHit(1), factoryClockHit(2), factoryClockHit(3), factoryClockHit(4))
        panel.addCompletedRound(dartsThrown)

        val achievement = AchievementEntity.retrieveAchievement(AchievementType.CLOCK_BEST_STREAK, playerId)!!
        achievement.achievementCounter shouldBe 4
        achievement.gameIdEarned shouldBe panel.getGameId()

        val roundTwo = listOf(factoryClockHit(5), Dart(20, 1), Dart(20, 1))
        panel.addCompletedRound(roundTwo)

        val updatedAchievement = AchievementEntity.retrieveAchievement(AchievementType.CLOCK_BEST_STREAK, playerId)!!
        updatedAchievement.achievementCounter shouldBe 5
    }

    @Test
    fun `Should update the dartboard when ready for throw`()
    {
        val panel = makeRoundTheClockGamePanel()
        panel.readyForThrow()
        panel.dartboard.segmentStatuses()!!.scoringSegments.shouldContainExactly(getAllNonMissSegments().filter { it.score == 1 })
    }

    @Test
    fun `Should update the dartboard when a dart is thrown`()
    {
        val panel = makeRoundTheClockGamePanel()
        panel.dartThrown(Dart(1, 1))
        panel.dartboard.segmentStatuses()!!.scoringSegments.shouldContainExactly(getAllNonMissSegments().filter { it.score == 2 })
    }

    /**
     * Team achievements
     */
    @Test
    fun `Should unlock the correct achievements for team play`()
    {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = makeRoundTheClockGamePanel(team)
        val gameId = panel.gameEntity.rowId

        val roundOne = listOf(factoryClockHit(1), factoryClockHit(2), factoryClockHit(3), factoryClockHit(4))
        panel.addCompletedRound(roundOne)

        val roundTwo = listOf(factoryClockHit(5), factoryClockHit(6), factoryClockHit(7), factoryClockHit(8))
        panel.addCompletedRound(roundTwo)

        retrieveAchievementsForPlayer(p1.rowId).shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, gameId, "1"),
        )

        retrieveAchievementsForPlayer(p2.rowId).shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, gameId, "2"),
        )
    }

    private fun factoryClockHit(clockTarget: Int): Dart
    {
        val dart = Dart(clockTarget, 1)
        dart.startingScore = clockTarget
        return dart
    }
}