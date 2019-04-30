package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementX01Shanghai
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01Shanghai: TestAbstractAchievementRowPerGame<AchievementX01Shanghai>()
{
    override fun factoryAchievement() = AchievementX01Shanghai()

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()
        insertSuccessForGameAndPlayer(g, p)
    }

    @Test
    fun `Should not count misses`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 3, ordinal = 2, startingScore = 340)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 0, ordinal = 3, startingScore = 280)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should count shanghais in any order`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 1, ordinal = 2, startingScore = 340)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 2, ordinal = 3, startingScore = 320)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GAME_TYPE_GOLF)
        insertSuccessForGameAndPlayer(g, p)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds of only two darts`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, score = 20, multiplier = 3, ordinal = 1, startingScore = 400)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 3, ordinal = 2, startingScore = 340)

        factoryAchievement().populateForConversion("")
        getCountFromTable("Achievement") shouldBe 0
    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)
    private fun insertSuccessForGameAndPlayer(g: GameEntity, p: PlayerEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, score = 20, multiplier = 2, ordinal = 1, startingScore = 400)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 3, ordinal = 2, startingScore = 360)
        insertDart(roundId = rnd.rowId, score = 20, multiplier = 1, ordinal = 3, startingScore = 300)
    }
}