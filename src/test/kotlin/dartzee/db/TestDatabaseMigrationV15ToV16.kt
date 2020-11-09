package dartzee.db

import dartzee.core.util.getSqlDateNow
import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import org.junit.Test
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

class TestDatabaseMigrationV15ToV16: AbstractTest()
{
    @Test
    fun `V15 - V16 should create SyncAudit table and add DtAchieved column`()
    {
        withV15Database { database ->
            val migrator = DatabaseMigrator(DatabaseMigrations.getConversionsMap())
            migrator.migrateToLatest(database, "Test")

            database.getDatabaseVersion() shouldBe 16

            shouldNotThrowAny {
                SyncAuditEntity(database).retrieveForId("foo", false)
            }
        }
    }

    @Test
    fun `V15 - V16 should convert to new achievement schema`()
    {

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

    //Other stuff
    var localGameIdEarned = -1L

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
        fun retrieveAchievements(playerId: String): MutableList<AchievementEntityOld>
        {
            val achievements = mutableListOf<AchievementEntityOld>()
            val dao = AchievementEntityOld()

            val sb = StringBuilder()
            sb.append("SELECT ${dao.getColumnsForSelectStatement("a")}, ")
            sb.append(" CASE WHEN g.LocalId IS NULL THEN -1 ELSE g.LocalId END AS LocalGameId")
            sb.append(" FROM Achievement a")
            sb.append(" LEFT OUTER JOIN Game g ON (a.GameIdEarned = g.RowId)")
            sb.append(" WHERE PlayerId = '$playerId'")

            InjectedThings.mainDatabase.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val entity = dao.factoryFromResultSet(rs)
                    entity.localGameIdEarned = rs.getLong("LocalGameId")

                    achievements.add(entity)
                }
            }

            return achievements
        }


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