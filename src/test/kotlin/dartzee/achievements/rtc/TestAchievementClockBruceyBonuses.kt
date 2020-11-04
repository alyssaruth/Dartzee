package dartzee.achievements.rtc

import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
import dartzee.helper.*
import dartzee.utils.Database
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementClockBruceyBonuses: AbstractMultiRowAchievementTest<AchievementClockBruceyBonuses>()
{
    override fun factoryAchievement() = AchievementClockBruceyBonuses()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1, database = database)
    }

    override fun insertRelevantGame(dtLastUpdate: Timestamp, database: Database): GameEntity
    {
        return insertGame(gameType = factoryAchievement().gameType,
            gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson(),
            dtLastUpdate = dtLastUpdate,
            database = database)
    }

    @Test
    fun `Should ignore count darts that dont have an ordinal of 4`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 3, startingScore = 3, score = 3, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should count all non-misses for a standard game, and include the roundNumber`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1, roundNumber = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2, roundNumber = 4)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3, roundNumber = 8)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 3

        val achievements = retrieveAchievementsForPlayer(p.rowId)
        achievements.shouldContainExactlyInAnyOrder(
                AchievementSummary(ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES, -1, g.rowId, "1"),
                AchievementSummary(ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES, -1, g.rowId, "4"),
                AchievementSummary(ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES, -1, g.rowId, "8")
        )
    }

    @Test
    fun `Should count doubles for a doubles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Doubles, true).toJson())

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should count trebles for a trebles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Trebles, true).toJson())

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should not count misses for a standard game`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count non-doubles for a doubles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Doubles, true).toJson())

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count non-trebles for a trebles game`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = RoundTheClockConfig(ClockType.Trebles, true).toJson())

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count darts that hit the wrong target`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 3, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should use the dart DtCreation to set DtLastUpdate on the rows`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1, dtCreation = Timestamp(500))
        insertDart(pt, ordinal = 4, startingScore = 3, score = 3, multiplier = 1, dtCreation = Timestamp(2000))
        insertDart(pt, ordinal = 4, startingScore = 9, score = 9, multiplier = 1, dtCreation = Timestamp(1500))

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 3
        val dtLastUpdates = AchievementEntity().retrieveEntities().map { it.dtLastUpdate }
        dtLastUpdates.shouldContainExactlyInAnyOrder(Timestamp(500), Timestamp(2000), Timestamp(1500))
    }

}