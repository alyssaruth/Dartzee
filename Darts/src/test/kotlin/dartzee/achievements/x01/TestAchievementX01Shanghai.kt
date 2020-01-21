package dartzee.test.achievements.x01

import dartzee.achievements.x01.AchievementX01Shanghai
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.test.achievements.TestAbstractAchievementRowPerGame
import dartzee.test.helper.getCountFromTable
import dartzee.test.helper.insertDart
import dartzee.test.helper.insertParticipant
import dartzee.test.helper.insertPlayer
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