package dartzee.achievements.rtc

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.factoryClockHit
import dartzee.helper.factoryClockMiss
import dartzee.helper.insertDart
import dartzee.helper.insertDarts
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestAchievementClockBestStreak: AbstractAchievementTest<AchievementClockBestStreak>()
{
    override fun factoryAchievement() = AchievementClockBestStreak()

    @Test
    fun `Should ignore streaks executed as part of a team`()
    {
        val pt = insertRelevantParticipant(team = true)
        insertOpeningStreak(pt)

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should reset the streak across game boundaries, and should report on the earliest occurrence`()
    {
        val g = insertRelevantGame(Timestamp(500))
        val g2 = insertRelevantGame(Timestamp(1000))

        val p = insertPlayer()

        insertOpeningStreak(p, g)
        insertOpeningStreak(p, g2)

        runConversion()

        val achievement = AchievementEntity.retrieveAchievement(factoryAchievement().achievementType, p.rowId)!!
        achievement.achievementCounter shouldBe 3
        achievement.gameIdEarned shouldBe g.rowId
    }

    @Test
    fun `Should correctly identify a more complex streak`()
    {
        val pt = insertRelevantParticipant()

        val roundOne = listOf(factoryClockHit(1), factoryClockHit(2), factoryClockMiss(3))
        val roundTwo = listOf(factoryClockHit(3), factoryClockHit(4), factoryClockHit(5), factoryClockHit(6))
        val roundThree = listOf(factoryClockHit(7), factoryClockMiss(8), factoryClockHit(8))

        roundOne.insertDarts(pt, 1)
        roundTwo.insertDarts(pt, 2)
        roundThree.insertDarts(pt, 3)

        runConversion()

        val achievement = AchievementEntity.retrieveAchievement(factoryAchievement().achievementType, pt.playerId)!!
        achievement.achievementCounter shouldBe 5
        achievement.gameIdEarned shouldBe pt.gameId
    }

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertOpeningStreak(p, g, database)
    }

    private fun insertOpeningStreak(p: PlayerEntity, g: GameEntity, database: Database = mainDatabase)
    {
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, database = database)
        insertOpeningStreak(pt, database)
    }
    private fun insertOpeningStreak(pt: ParticipantEntity, database: Database = mainDatabase)
    {
        insertDart(pt, roundNumber = 1, ordinal = 1, startingScore = 1, score = 1, multiplier = 1, database = database)
        insertDart(pt, roundNumber = 1, ordinal = 2, startingScore = 2, score = 2, multiplier = 1, database = database)
        insertDart(pt, roundNumber = 1, ordinal = 3, startingScore = 3, score = 3, multiplier = 1, database = database)
    }

}