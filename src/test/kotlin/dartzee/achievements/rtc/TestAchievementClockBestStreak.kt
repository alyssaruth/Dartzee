package dartzee.achievements.rtc

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementClockBestStreak: AbstractAchievementTest<AchievementClockBestStreak>()
{
    override fun factoryAchievement() = AchievementClockBestStreak()

    @Test
    fun `Should reset the streak across game boundaries, and should report on the earliest occurrence`()
    {
        val g = insertRelevantGame(Timestamp(500))
        val g2 = insertRelevantGame(Timestamp(1000))

        val p = insertPlayer()

        insertOpeningStreak(p, g)
        insertOpeningStreak(p, g2)

        factoryAchievement().populateForConversion("")

        val achievement = AchievementEntity.retrieveAchievement(factoryAchievement().achievementRef, p.rowId)!!
        achievement.achievementCounter shouldBe 3
        achievement.gameIdEarned shouldBe g.rowId
    }

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        insertOpeningStreak(p, g)
    }

    private fun insertOpeningStreak(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId)

        insertDart(pt, roundNumber = 1, ordinal = 1, startingScore = 1, score = 1, multiplier = 1)
        insertDart(pt, roundNumber = 1, ordinal = 2, startingScore = 2, score = 2, multiplier = 1)
        insertDart(pt, roundNumber = 1, ordinal = 3, startingScore = 3, score = 3, multiplier = 1)
    }
}