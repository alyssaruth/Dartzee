package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01Shanghai
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.TestAbstractAchievementRowPerGame
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01Shanghai: TestAbstractAchievementRowPerGame<AchievementX01Shanghai>()
{
    override fun factoryAchievement() = AchievementX01Shanghai()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, score = 20, multiplier = 2, ordinal = 1, startingScore = 400)
        insertDart(pt, score = 20, multiplier = 3, ordinal = 2, startingScore = 360)
        insertDart(pt, score = 20, multiplier = 1, ordinal = 3, startingScore = 300)
    }

    @Test
    fun `Should not count misses`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(pt, score = 20, multiplier = 3, ordinal = 2, startingScore = 340)
        insertDart(pt, score = 20, multiplier = 0, ordinal = 3, startingScore = 280)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should count shanghais in any order`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(pt, score = 20, multiplier = 1, ordinal = 2, startingScore = 340)
        insertDart(pt, score = 20, multiplier = 2, ordinal = 3, startingScore = 320)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should ignore rounds of only two darts`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(pt, score = 20, multiplier = 3, ordinal = 2, startingScore = 340)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 0
    }
}