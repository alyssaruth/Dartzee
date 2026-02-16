package dartzee.achievements.golf

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.drtDoubleEighteen
import dartzee.drtDoubleFour
import dartzee.drtDoubleTen
import dartzee.drtDoubleThree
import dartzee.drtDoubleTwenty
import dartzee.drtInnerFive
import dartzee.drtInnerFourteen
import dartzee.drtInnerSixteen
import dartzee.drtInnerTwelve
import dartzee.drtMissEight
import dartzee.drtMissFive
import dartzee.drtMissNine
import dartzee.drtMissOne
import dartzee.drtMissSeventeen
import dartzee.drtMissSix
import dartzee.drtMissSixteen
import dartzee.drtOuterEight
import dartzee.drtOuterEleven
import dartzee.drtOuterFifteen
import dartzee.drtOuterNine
import dartzee.drtOuterOne
import dartzee.drtOuterSeven
import dartzee.drtOuterSeventeen
import dartzee.drtOuterSix
import dartzee.drtOuterThirteen
import dartzee.drtOuterTwelve
import dartzee.drtTrebleTwo
import dartzee.helper.insertDart
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.makeGolfRounds
import dartzee.only
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

class TestAchievementGolfInBounds : AbstractAchievementTest<AchievementGolfInBounds>() {
    override fun factoryAchievement() = AchievementGolfInBounds()

    override fun insertRelevantGame(dtLastUpdate: Timestamp, database: Database) =
        insertGame(
            gameType = factoryAchievement().gameType,
            gameParams = "18",
            dtLastUpdate = dtLastUpdate,
            database = database,
        )

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database,
    ) {
        val pt =
            insertParticipant(
                playerId = p.rowId,
                gameId = g.rowId,
                finalScore = 54,
                database = database,
            )
        setUpDartsForParticipant(pt, database)
    }

    private fun factorySuccessRounds() =
        listOf(
            listOf(drtMissOne(), drtOuterOne()), // 4
            listOf(drtTrebleTwo()), // 2
            listOf(drtDoubleThree()), // 1
            listOf(drtDoubleFour()), // 1
            listOf(drtMissFive(), drtMissFive(), drtInnerFive()), // 3
            listOf(drtMissSix(), drtOuterSix()), // 4
            listOf(drtOuterSeven()), // 4
            listOf(drtMissEight(), drtOuterSeven(), drtOuterEight()), // 4
            listOf(drtMissNine(), drtOuterTwelve(), drtOuterNine()), // 4
            // Halfway - 27
            listOf(drtDoubleTen()), // 1
            listOf(drtOuterEleven()), // 4
            listOf(drtInnerTwelve()), // 3
            listOf(drtOuterThirteen()), // 4
            listOf(drtInnerFourteen()), // 3
            listOf(drtOuterFifteen()), // 4
            listOf(drtMissSixteen(), drtMissSixteen(), drtInnerSixteen()), // 3
            listOf(drtMissSeventeen(), drtOuterSeventeen()), // 4
            listOf(drtDoubleEighteen()), // 1
        )

    private fun setUpDartsForParticipant(pt: ParticipantEntity, database: Database = mainDatabase) {
        val golfRounds = makeGolfRounds(factorySuccessRounds())
        golfRounds.flatten().forEach { insertDart(pt, it, database = database) }
    }

    @Test
    fun `Should populate with the correct score and gameId`() {
        val g = insertRelevantGame()
        val p = insertPlayer()
        setUpAchievementRowForPlayerAndGame(p, g)

        runConversion()

        val achievement = AchievementEntity.retrieveAchievements(p.rowId).only()
        achievement.gameIdEarned shouldBe g.rowId
        achievement.achievementDetail shouldBe "54"
    }

    @Test
    fun `Should ignore 9 hole games`() {
        val g = insertGame(gameType = factoryAchievement().gameType, gameParams = "9")
        val p = insertPlayer()
        setUpAchievementRowForPlayerAndGame(p, g)

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore unfinished players`() {
        val g = insertRelevantGame()
        val p = insertPlayer()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = -1)
        setUpDartsForParticipant(pt)

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore players who were on a team`() {
        val g = insertRelevantGame()
        val p = insertPlayer()
        val pt =
            insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 54, teamId = "foo")
        setUpDartsForParticipant(pt)

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should reject games where a round ended with missing the board`() {
        val g = insertRelevantGame()
        val p = insertPlayer()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 56)

        val rounds = factorySuccessRounds().toMutableList()
        rounds[4] = listOf(drtMissFive(), drtMissFive(), drtMissFive())

        val golfRounds = makeGolfRounds(rounds)
        golfRounds.flatten().forEach { insertDart(pt, it) }

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should reject games where a round ended with hitting a different number`() {
        val g = insertRelevantGame()
        val p = insertPlayer()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 56)

        val rounds = factorySuccessRounds().toMutableList()
        rounds[0] = listOf(drtMissOne(), drtOuterOne(), drtDoubleTwenty())

        val golfRounds = makeGolfRounds(rounds)
        golfRounds.flatten().forEach { insertDart(pt, it) }

        runConversion()

        getAchievementCount() shouldBe 0
    }
}
