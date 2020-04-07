package dartzee.achievements.rtc

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.*
import dartzee.helper.*
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementClockBruceyBonuses: AbstractAchievementTest<AchievementClockBruceyBonuses>()
{
    override fun factoryAchievement() = AchievementClockBruceyBonuses()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
    }

    override fun insertRelevantGame(dtLastUpdate: Timestamp): GameEntity
    {
        return insertGame(gameType = factoryAchievement().gameType, gameParams = CLOCK_TYPE_STANDARD, dtLastUpdate = dtLastUpdate)
    }

    @Test
    fun `Should ignore count darts that dont have an ordinal of 4`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 3, startingScore = 3, score = 3, multiplier = 1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should count all non-misses for a standard game`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        retrieveAchievement().achievementCounter shouldBe 3
    }

    @Test
    fun `Should count doubles for a doubles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = CLOCK_TYPE_DOUBLES)

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        retrieveAchievement().achievementCounter shouldBe 1
    }

    @Test
    fun `Should count trebles for a trebles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = CLOCK_TYPE_TREBLES)

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        retrieveAchievement().achievementCounter shouldBe 1
    }

    @Test
    fun `Should not count misses for a standard game`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count non-doubles for a doubles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = CLOCK_TYPE_DOUBLES)

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count non-trebles for a trebles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = CLOCK_TYPE_TREBLES)

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count darts that hit the wrong target`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 3, multiplier = 1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should add up the examples per player and set the correct DtLastUpdate`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1, dtCreation = Timestamp(500))
        insertDart(pt, ordinal = 4, startingScore = 3, score = 3, multiplier = 1, dtCreation = Timestamp(2000))
        insertDart(pt, ordinal = 4, startingScore = 9, score = 9, multiplier = 1, dtCreation = Timestamp(1500))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val a = retrieveAchievement()

        a.playerId shouldBe p.rowId
        a.gameIdEarned shouldBe ""
        a.achievementDetail shouldBe ""
        a.achievementCounter shouldBe 3
        a.dtLastUpdate shouldBe Timestamp(2000)
    }

}