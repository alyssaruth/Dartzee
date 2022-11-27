package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01Shanghai: AbstractMultiRowAchievementTest<AchievementX01Shanghai>()
{
    override fun factoryAchievement() = AchievementX01Shanghai()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        insertDart(pt, score = 20, multiplier = 2, ordinal = 1, startingScore = 400, database = database)
        insertDart(pt, score = 20, multiplier = 3, ordinal = 2, startingScore = 360, database = database)
        insertDart(pt, score = 20, multiplier = 1, ordinal = 3, startingScore = 300, database = database)
    }

    @Test
    fun `Should count shanghais thrown when part of a team`()
    {
        val pt = insertRelevantParticipant(team = true)

        insertDart(pt, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(pt, score = 20, multiplier = 1, ordinal = 2, startingScore = 340)
        insertDart(pt, score = 20, multiplier = 2, ordinal = 3, startingScore = 320)

        factoryAchievement().populateForConversion(emptyList())
        getCountFromTable("Achievement") shouldBe 1
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

        factoryAchievement().populateForConversion(emptyList())
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

        factoryAchievement().populateForConversion(emptyList())
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

        factoryAchievement().populateForConversion(emptyList())
        getCountFromTable("Achievement") shouldBe 0
    }
}