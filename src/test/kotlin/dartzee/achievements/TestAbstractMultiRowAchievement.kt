package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.logging.CODE_MERGE_ROW_SKIPPED
import dartzee.shouldContainKeyValues
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import io.kotlintest.shouldBe
import org.junit.Test
import java.util.*

private const val ACHIEVEMENT_REF = 56

class TestAbstractMultiRowAchievement: AbstractTest()
{
    private val playerId = UUID.randomUUID().toString()
    private val gameId = UUID.randomUUID().toString()

    @Test
    fun `Should not insert a duplicate row`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val remoteRow = insertAchievement(database = otherDb, achievementRef = ACHIEVEMENT_REF, achievementDetail = "detail", gameIdEarned = gameId, playerId = playerId)

            val existingAchievement = AchievementEntity()
            existingAchievement.assignRowId()
            existingAchievement.achievementRef = ACHIEVEMENT_REF
            existingAchievement.achievementDetail = "detail"
            existingAchievement.gameIdEarned = gameId
            existingAchievement.playerId = playerId

            ExampleAchievement().mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            getCountFromTable("Achievement", otherDb) shouldBe 1

            val log = verifyLog(CODE_MERGE_ROW_SKIPPED)
            log.message shouldBe "Not merging achievement row as one already exists"
            log.shouldContainKeyValues("PlayerId" to playerId,
                "AchievementRef" to ACHIEVEMENT_REF,
                "GameIdEarned" to gameId,
                "AchievementDetail" to "detail")

            val result = retrieveAchievement(otherDb)
            result.rowId shouldBe remoteRow.rowId
        }
    }

    @Test
    fun `Should insert a row if achievement detail is different`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            insertAchievement(database = otherDb, achievementRef = ACHIEVEMENT_REF, achievementDetail = "detail", gameIdEarned = gameId, playerId = playerId)

            val existingAchievement = AchievementEntity()
            existingAchievement.assignRowId()
            existingAchievement.achievementRef = ACHIEVEMENT_REF
            existingAchievement.achievementDetail = "otherDetail"
            existingAchievement.gameIdEarned = gameId
            existingAchievement.playerId = playerId

            ExampleAchievement().mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            getCountFromTable("Achievement", otherDb) shouldBe 2
        }
    }

    @Test
    fun `Should insert a row if game ID is different`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            insertAchievement(database = otherDb, achievementRef = ACHIEVEMENT_REF, achievementDetail = "detail", gameIdEarned = gameId, playerId = playerId)

            val existingAchievement = AchievementEntity()
            existingAchievement.assignRowId()
            existingAchievement.achievementRef = ACHIEVEMENT_REF
            existingAchievement.achievementDetail = "detail"
            existingAchievement.gameIdEarned = UUID.randomUUID().toString()
            existingAchievement.playerId = playerId

            ExampleAchievement().mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            getCountFromTable("Achievement", otherDb) shouldBe 2
        }
    }

    @Test
    fun `Should insert a row if achievement ref is different`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            insertAchievement(database = otherDb, achievementRef = ACHIEVEMENT_REF + 1, achievementDetail = "detail", gameIdEarned = gameId, playerId = playerId)

            val existingAchievement = AchievementEntity()
            existingAchievement.assignRowId()
            existingAchievement.achievementRef = ACHIEVEMENT_REF
            existingAchievement.achievementDetail = "detail"
            existingAchievement.gameIdEarned = gameId
            existingAchievement.playerId = playerId

            ExampleAchievement().mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            getCountFromTable("Achievement", otherDb) shouldBe 2
        }
    }

    @Test
    fun `Should insert a row if playerId is different`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            insertAchievement(database = otherDb, achievementRef = ACHIEVEMENT_REF, achievementDetail = "detail", gameIdEarned = gameId, playerId = playerId)

            val existingAchievement = AchievementEntity()
            existingAchievement.assignRowId()
            existingAchievement.achievementRef = ACHIEVEMENT_REF
            existingAchievement.achievementDetail = "detail"
            existingAchievement.gameIdEarned = gameId
            existingAchievement.playerId = UUID.randomUUID().toString()

            ExampleAchievement().mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            getCountFromTable("Achievement", otherDb) shouldBe 2
        }
    }

    class ExampleAchievement(private val decreasing: Boolean = false): AbstractMultiRowAchievement()
    {
        override val name = "Example"
        override val desc = "An example achievement"
        override val achievementRef = ACHIEVEMENT_REF
        override val redThreshold = 1
        override val orangeThreshold = 2
        override val yellowThreshold = 3
        override val greenThreshold = 4
        override val blueThreshold = 5
        override val pinkThreshold = 6
        override val maxValue = 6
        override val gameType = GameType.X01

        override fun populateForConversion(playerIds: String, database: Database) {}

        override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_BEST_FINISH

        override fun isDecreasing() = decreasing

        override fun getBreakdownColumns() = listOf("Game", "Date Achieved")
        override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.localGameIdEarned, a.dtLastUpdate)
    }
}