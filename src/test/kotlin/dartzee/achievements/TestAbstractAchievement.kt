package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.retrieveAchievement
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp
import java.util.*

private const val ACHIEVEMENT_REF = 42

class TestAbstractAchievement: AbstractTest()
{
    private val playerId = UUID.randomUUID().toString()
    private val localGameId = UUID.randomUUID().toString()
    private val remoteGameId = UUID.randomUUID().toString()

    @Test
    fun `Should insert into the database if no remote row`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val existingAchievement = insertLocalAchievement(achievementCounter = 4, dtLastUpdate = Timestamp(500))

            val achievementDto = ExampleAchievement()
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 4
            result.dtLastUpdate shouldBe Timestamp(500)
            result.gameIdEarned shouldBe localGameId
        }
    }

    @Test
    fun `If local row is older and same achievementCounter, local should win`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val remoteTime = Timestamp(10000)
            val localTime = Timestamp(500)

            insertRemoteAchievement(achievementCounter = 4, dtLastUpdate = remoteTime, database = otherDb)
            val existingAchievement = insertLocalAchievement(achievementCounter = 4, dtLastUpdate = localTime)

            val achievementDto = ExampleAchievement()
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 4
            result.dtLastUpdate shouldBe localTime
            result.gameIdEarned shouldBe localGameId
        }
    }

    @Test
    fun `If local row is newer and same achievementCounter, local should win`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val remoteTime = Timestamp(500)
            val localTime = Timestamp(10000)

            insertRemoteAchievement(achievementCounter = 4, dtLastUpdate = remoteTime, database = otherDb)
            val existingAchievement = insertLocalAchievement(achievementCounter = 4, dtLastUpdate = localTime)

            val achievementDto = ExampleAchievement()
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 4
            result.dtLastUpdate shouldBe remoteTime
            result.gameIdEarned shouldBe remoteGameId
        }
    }

    @Test
    fun `If increasing and local row is higher, local should win`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val dtLastUpdate = Timestamp(500)

            insertRemoteAchievement(achievementCounter = 4, dtLastUpdate = dtLastUpdate, database = otherDb)
            val existingAchievement = insertLocalAchievement(achievementCounter = 6, dtLastUpdate = dtLastUpdate)

            val achievementDto = ExampleAchievement(decreasing = false)
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 6
            result.gameIdEarned shouldBe localGameId
        }
    }

    @Test
    fun `If increasing and local row is lower, remote should win`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val dtLastUpdate = Timestamp(500)

            insertRemoteAchievement(achievementCounter = 6, dtLastUpdate = dtLastUpdate, database = otherDb)
            val existingAchievement = insertLocalAchievement(achievementCounter = 4, dtLastUpdate = dtLastUpdate)

            val achievementDto = ExampleAchievement(decreasing = false)
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 6
            result.gameIdEarned shouldBe remoteGameId
        }
    }

    @Test
    fun `If decreasing and local row is higher, remote should win`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val dtLastUpdate = Timestamp(500)

            insertRemoteAchievement(achievementCounter = 4, dtLastUpdate = dtLastUpdate, database = otherDb)
            val existingAchievement = insertLocalAchievement(achievementCounter = 6, dtLastUpdate = dtLastUpdate)

            val achievementDto = ExampleAchievement(decreasing = true)
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 4
            result.gameIdEarned shouldBe remoteGameId
        }
    }

    @Test
    fun `If decreasing and local row is lower, local should win`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDb ->
            val otherDao = AchievementEntity(otherDb)

            val dtLastUpdate = Timestamp(500)

            insertRemoteAchievement(achievementCounter = 6, dtLastUpdate = dtLastUpdate, database = otherDb)
            val existingAchievement = insertLocalAchievement(achievementCounter = 4, dtLastUpdate = dtLastUpdate)

            val achievementDto = ExampleAchievement(decreasing = true)
            achievementDto.mergeIntoOtherDatabase(existingAchievement, otherDao, otherDb)

            val result = retrieveAchievement(otherDb)
            result.achievementCounter shouldBe 4
            result.gameIdEarned shouldBe localGameId
        }
    }

    private fun insertLocalAchievement(achievementCounter: Int, dtLastUpdate: Timestamp) =
        insertAchievement(achievementRef = ACHIEVEMENT_REF,
                playerId = playerId,
                gameIdEarned = localGameId,
                achievementCounter = achievementCounter,
                dtLastUpdate = dtLastUpdate)

    private fun insertRemoteAchievement(achievementCounter: Int, dtLastUpdate: Timestamp, database: Database) =
            insertAchievement(achievementRef = ACHIEVEMENT_REF,
                    playerId = playerId,
                    gameIdEarned = remoteGameId,
                    achievementCounter = achievementCounter,
                    dtLastUpdate = dtLastUpdate,
                    database = database)


    class ExampleAchievement(private val decreasing: Boolean = false): AbstractAchievement()
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
    }
}