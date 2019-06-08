package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK
import burlton.dartzee.code.achievements.x01.AchievementX01SuchBadLuck
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementX01SuchBadLuck: AbstractAchievementTest<AchievementX01SuchBadLuck>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01SuchBadLuck()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 1, startingScore = 2, score = 20, multiplier = 2)
    }

    @Test
    fun `Should report on the highest streak per player`()
    {
        val p = insertPlayer()

        val g1 = insertRelevantGame()
        val g2 = insertRelevantGame()
        val pt1 = insertParticipant(playerId = p.rowId, gameId = g1.rowId)
        val pt2 = insertParticipant(playerId = p.rowId, gameId = g2.rowId)

        //Scores 2 in first game
        insertDart(pt1, ordinal = 1, startingScore = 2, score = 20, multiplier = 2)
        insertDart(pt1, ordinal = 2, startingScore = 2, score = 18, multiplier = 2)

        //Scores 3 in second game
        insertDart(pt2, ordinal = 1, startingScore = 2, score = 20, multiplier = 2)
        insertDart(pt2, ordinal = 2, startingScore = 2, score = 18, multiplier = 2)
        insertDart(pt2, ordinal = 3, startingScore = 2, score = 20, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 1
        val achievement = AchievementEntity().retrieveEntities("").first()
        achievement.achievementRef shouldBe ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK
        achievement.achievementCounter shouldBe 3
        achievement.gameIdEarned shouldBe g2.rowId
        achievement.playerId shouldBe p.rowId
        achievement.achievementDetail shouldBe ""
    }

    @Test
    fun `Should include near-miss bullseyes`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        //Scores 2 in first game
        insertDart(pt, ordinal = 1, startingScore = 50, score = 25, multiplier = 1)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 1
        val achievement = AchievementEntity().retrieveEntities("").first()
        achievement.achievementCounter shouldBe 1
        achievement.gameIdEarned shouldBe g.rowId
        achievement.playerId shouldBe p.rowId
    }

    @Test
    fun `Should report the earliest example of the highest streak`()
    {
        val p = insertPlayer()

        val g1 = insertRelevantGame()
        val g2 = insertRelevantGame()
        val pt1 = insertParticipant(playerId = p.rowId, gameId = g1.rowId)
        val pt2 = insertParticipant(playerId = p.rowId, gameId = g2.rowId)

        //Scores 2 in first game
        insertDart(pt1, ordinal = 1, startingScore = 2, score = 20, multiplier = 2, dtLastUpdate = Timestamp(500))
        insertDart(pt1, ordinal = 2, startingScore = 2, score = 18, multiplier = 2, dtLastUpdate = Timestamp(800))

        //Scores 3 in second game
        insertDart(pt2, ordinal = 1, startingScore = 2, score = 20, multiplier = 2, dtLastUpdate = Timestamp(200))
        insertDart(pt2, ordinal = 2, startingScore = 2, score = 18, multiplier = 2, dtLastUpdate = Timestamp(250))

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 1
        val achievement = AchievementEntity().retrieveEntities("").first()
        achievement.achievementCounter shouldBe 2
        achievement.gameIdEarned shouldBe g2.rowId
        achievement.playerId shouldBe p.rowId
        achievement.achievementDetail shouldBe ""
        achievement.dtLastUpdate shouldBe Timestamp(250)
    }
}