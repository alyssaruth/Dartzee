package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementClockBestStreak
import burlton.dartzee.code.db.*
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementClockBestStreak: AbstractAchievementTest<AchievementClockBestStreak>()
{
    override fun factoryAchievement() = AchievementClockBestStreak()

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val g = insertGame(gameType = GAME_TYPE_X01)
        val p = insertPlayer()
        insertOpeningStreak(p, g)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should reset the streak across game boundaries, and should report on the earliest occurrence`()
    {
        val g = insertRelevantGame()
        val g2 = insertRelevantGame()

        val p = insertPlayer()

        insertOpeningStreak(p, g)
        insertOpeningStreak(p, g2)

        factoryAchievement().populateForConversion("")

        val achievement = AchievementEntity.retrieveAchievement(factoryAchievement().achievementRef, p.rowId)!!
        achievement.achievementCounter shouldBe 3
        achievement.gameIdEarned shouldBe g.rowId
    }

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()

        insertOpeningStreak(p, g)
    }

    private fun insertOpeningStreak(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId)

        val rnd = insertRound(roundNumber = 1, participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, ordinal = 1, startingScore = 1, score = 1, multiplier = 1)
        insertDart(roundId = rnd.rowId, ordinal = 2, startingScore = 2, score = 2, multiplier = 1)
        insertDart(roundId = rnd.rowId, ordinal = 3, startingScore = 3, score = 3, multiplier = 1)
    }


    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_ROUND_THE_CLOCK)
}