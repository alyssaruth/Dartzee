package dartzee.achievements.rtc

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
import dartzee.helper.AchievementSummary
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDart
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.utils.Database
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

class TestAchievementClockBruceyBonuses :
    AbstractMultiRowAchievementTest<AchievementClockBruceyBonuses>() {
    override fun factoryAchievement() = AchievementClockBruceyBonuses()

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database
    ) {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        insertDart(
            pt,
            ordinal = 4,
            startingScore = 4,
            score = 4,
            multiplier = 1,
            database = database
        )
    }

    override fun insertRelevantGame(dtLastUpdate: Timestamp, database: Database): GameEntity {
        return insertGame(
            gameType = factoryAchievement().gameType,
            gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson(),
            dtLastUpdate = dtLastUpdate,
            database = database
        )
    }

    @Test
    fun `Should include participants who were part of a team`() {
        val pt = insertRelevantParticipant(team = true)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)

        runConversion()

        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should ignore count darts that dont have an ordinal of 4`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 3, startingScore = 3, score = 3, multiplier = 1)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should count all non-misses for a standard game, and include the roundNumber`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1, roundNumber = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2, roundNumber = 4)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3, roundNumber = 8)

        runConversion()

        getCountFromTable("Achievement") shouldBe 3

        val achievements = retrieveAchievementsForPlayer(p.rowId)
        achievements.shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, g.rowId, "1"),
            AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, g.rowId, "4"),
            AchievementSummary(AchievementType.CLOCK_BRUCEY_BONUSES, -1, g.rowId, "8")
        )
    }

    @Test
    fun `Should count doubles for a doubles game`() {
        val p = insertPlayer()
        val g =
            insertGame(
                gameType = GameType.ROUND_THE_CLOCK,
                gameParams = RoundTheClockConfig(ClockType.Doubles, true).toJson()
            )

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)

        runConversion()

        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should count trebles for a trebles game`() {
        val p = insertPlayer()
        val g =
            insertGame(
                gameType = GameType.ROUND_THE_CLOCK,
                gameParams = RoundTheClockConfig(ClockType.Trebles, true).toJson()
            )

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        runConversion()

        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should not count misses for a standard game`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count non-doubles for a doubles game`() {
        val p = insertPlayer()
        val g =
            insertGame(
                gameType = GameType.ROUND_THE_CLOCK,
                gameParams = RoundTheClockConfig(ClockType.Doubles, true).toJson()
            )

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 3)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count non-trebles for a trebles game`() {
        val p = insertPlayer()
        val g =
            insertGame(
                gameType = GameType.ROUND_THE_CLOCK,
                gameParams = RoundTheClockConfig(ClockType.Trebles, true).toJson()
            )

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 0)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
        insertDart(pt, ordinal = 4, startingScore = 4, score = 4, multiplier = 2)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should not count darts that hit the wrong target`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 4, startingScore = 4, score = 3, multiplier = 1)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should use the dart DtCreation to set DtAchieved on the rows`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(
            pt,
            ordinal = 4,
            startingScore = 4,
            score = 4,
            multiplier = 1,
            dtCreation = Timestamp(500)
        )
        insertDart(
            pt,
            ordinal = 4,
            startingScore = 3,
            score = 3,
            multiplier = 1,
            dtCreation = Timestamp(2000)
        )
        insertDart(
            pt,
            ordinal = 4,
            startingScore = 9,
            score = 9,
            multiplier = 1,
            dtCreation = Timestamp(1500)
        )

        runConversion()

        getCountFromTable("Achievement") shouldBe 3
        val dtLastUpdates = AchievementEntity().retrieveEntities().map { it.dtAchieved }
        dtLastUpdates.shouldContainExactlyInAnyOrder(
            Timestamp(500),
            Timestamp(2000),
            Timestamp(1500)
        )
    }
}
