package dartzee.db

import dartzee.achievements.AchievementType
import dartzee.core.util.getSqlDateNow
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.randomGuid
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import org.junit.jupiter.api.Test
import java.sql.Timestamp

private const val ACHIEVEMENT_REF_X01_BEST_FINISH = 0
private const val ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE = 1
private const val ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS = 2
private const val ACHIEVEMENT_REF_X01_HIGHEST_BUST = 3
private const val ACHIEVEMENT_REF_GOLF_POINTS_RISKED = 4
private const val ACHIEVEMENT_REF_X01_GAMES_WON = 5
private const val ACHIEVEMENT_REF_GOLF_GAMES_WON = 6
private const val ACHIEVEMENT_REF_CLOCK_GAMES_WON = 7
private const val ACHIEVEMENT_REF_X01_BEST_GAME = 8
private const val ACHIEVEMENT_REF_GOLF_BEST_GAME = 9
private const val ACHIEVEMENT_REF_CLOCK_BEST_GAME = 10
private const val ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES = 11
private const val ACHIEVEMENT_REF_X01_SHANGHAI = 12
private const val ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR = 13
private const val ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK = 14
private const val ACHIEVEMENT_REF_X01_BTBF = 15
private const val ACHIEVEMENT_REF_CLOCK_BEST_STREAK = 16
private const val ACHIEVEMENT_REF_X01_NO_MERCY = 17
private const val ACHIEVEMENT_REF_GOLF_COURSE_MASTER = 18
private const val ACHIEVEMENT_REF_DARTZEE_GAMES_WON = 19

private val hmOldRefToNewType = mapOf(
    ACHIEVEMENT_REF_X01_BEST_FINISH to AchievementType.X01_BEST_FINISH,
    ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE to AchievementType.X01_BEST_THREE_DART_SCORE,
    ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS to AchievementType.X01_CHECKOUT_COMPLETENESS,
    ACHIEVEMENT_REF_X01_HIGHEST_BUST to AchievementType.X01_HIGHEST_BUST,
    ACHIEVEMENT_REF_GOLF_POINTS_RISKED to AchievementType.GOLF_POINTS_RISKED,
    ACHIEVEMENT_REF_X01_GAMES_WON to AchievementType.X01_GAMES_WON,
    ACHIEVEMENT_REF_GOLF_GAMES_WON to AchievementType.GOLF_GAMES_WON,
    ACHIEVEMENT_REF_CLOCK_GAMES_WON to AchievementType.CLOCK_GAMES_WON,
    ACHIEVEMENT_REF_X01_BEST_GAME to AchievementType.X01_BEST_GAME,
    ACHIEVEMENT_REF_GOLF_BEST_GAME to AchievementType.GOLF_BEST_GAME,
    ACHIEVEMENT_REF_CLOCK_BEST_GAME to AchievementType.CLOCK_BEST_GAME,
    ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES to AchievementType.CLOCK_BRUCEY_BONUSES,
    ACHIEVEMENT_REF_X01_SHANGHAI to AchievementType.X01_SHANGHAI,
    ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR to AchievementType.X01_HOTEL_INSPECTOR,
    ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK to AchievementType.X01_SUCH_BAD_LUCK,
    ACHIEVEMENT_REF_X01_BTBF to AchievementType.X01_BTBF,
    ACHIEVEMENT_REF_CLOCK_BEST_STREAK to AchievementType.CLOCK_BEST_STREAK,
    ACHIEVEMENT_REF_X01_NO_MERCY to AchievementType.X01_NO_MERCY,
    ACHIEVEMENT_REF_GOLF_COURSE_MASTER to AchievementType.GOLF_COURSE_MASTER,
    ACHIEVEMENT_REF_DARTZEE_GAMES_WON to AchievementType.DARTZEE_GAMES_WON
)

class TestDatabaseMigrationV15ToV16: AbstractTest()
{
    @Test
    fun `V15 - V16 should create SyncAudit table and update Achievement schema`()
    {
        withV15Database { database ->
            runMigrationsForVersion(database, 15)

            shouldNotThrowAny {
                SyncAuditEntity(database).retrieveForId("foo", false)
                insertAchievement(database = database)
            }
        }
    }

    @Test
    fun `V15 - V16 should convert old refs to new types correctly`()
    {
        withV15Database { database ->
            val oldRefs = hmOldRefToNewType.keys
            val hmRefToOldAchievement = mutableMapOf<Int, AchievementEntityOld>()
            oldRefs.forEach { ref ->
                val oldEntity = AchievementEntityOld.factoryAndSave(ref, randomGuid(), randomGuid(), 10, database = database)
                hmRefToOldAchievement[ref] = oldEntity
            }

            DatabaseMigrations.runScript(database, 16, "1. Achievement.sql")

            oldRefs.forEach { ref ->
                val oldEntity = hmRefToOldAchievement.getValue(ref)
                val convertedAchievement = AchievementEntity(database).retrieveForId(oldEntity.rowId)!!
                convertedAchievement.achievementType shouldBe hmOldRefToNewType[ref]
                convertedAchievement.achievementCounter shouldBe oldEntity.achievementCounter
                convertedAchievement.gameIdEarned shouldBe oldEntity.gameIdEarned
                convertedAchievement.playerId shouldBe oldEntity.playerId
                convertedAchievement.dtLastUpdate shouldBe oldEntity.dtLastUpdate
                convertedAchievement.dtAchieved shouldBe oldEntity.dtLastUpdate
            }
        }
    }

    private fun withV15Database(testBlock: (inMemoryDatabase: Database) -> Unit)
    {
        usingInMemoryDatabase(withSchema = true) { database ->
            database.dropTable("SyncAudit")
            database.dropTable("Achievement")
            AchievementEntityOld(database).createTable()
            database.updateDatabaseVersion(15)
            testBlock(database)
        }
    }
}

private class AchievementEntityOld(database: Database = InjectedThings.mainDatabase) : AbstractEntity<AchievementEntityOld>(database)
{
    //DB Fields
    var playerId: String = ""
    var achievementRef = -1
    var gameIdEarned = ""
    var achievementCounter = -1
    var achievementDetail = ""
    var dtAchieved: Timestamp = getSqlDateNow()

    override fun getTableName() = "Achievement"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "AchievementRef INT NOT NULL, "
                + "GameIdEarned VARCHAR(36) NOT NULL, "
                + "AchievementCounter INT NOT NULL, "
                + "AchievementDetail VARCHAR(255) NOT NULL, "
                + "DtAchieved TIMESTAMP NOT NULL")
    }

    override fun includeInSync() = false

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "AchievementRef"))
    }

    companion object
    {
        fun retrieveAchievement(achievementRef: Int, playerId: String): AchievementEntityOld?
        {
            return AchievementEntityOld().retrieveEntity("PlayerId = '$playerId' AND AchievementRef = $achievementRef")
        }


        fun factory(achievementRef: Int,
                    playerId: String,
                    gameId: String,
                    counter: Int,
                    achievementDetail: String,
                    dtAchieved: Timestamp,
                    database: Database = InjectedThings.mainDatabase
        ): AchievementEntityOld
        {
            val ae = AchievementEntityOld(database)
            ae.assignRowId()
            ae.achievementRef = achievementRef
            ae.playerId = playerId
            ae.gameIdEarned = gameId
            ae.achievementCounter = counter
            ae.achievementDetail = achievementDetail
            ae.dtAchieved = dtAchieved
            return ae
        }

        fun factoryAndSave(achievementRef: Int,
                           playerId: String,
                           gameId: String,
                           counter: Int,
                           achievementDetail: String = "",
                           dtAchieved: Timestamp = getSqlDateNow(),
                           database: Database = InjectedThings.mainDatabase
        ): AchievementEntityOld
        {
            val ae = factory(achievementRef, playerId, gameId, counter, achievementDetail, dtAchieved, database)
            ae.saveToDatabase()
            return ae
        }
    }
}