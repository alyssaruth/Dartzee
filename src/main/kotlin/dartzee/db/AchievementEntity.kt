package dartzee.db

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.getAchievementForType
import dartzee.core.util.getSqlDateNow
import dartzee.screen.ScreenCache
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.Timestamp

/**
 * Entity to record a particular that a particular achievement has been earned by a player.
 *
 * Points at a GameId if applicable, and uses DtCreation as the "date unlocked".
 */
class AchievementEntity(database: Database = mainDatabase) : AbstractEntity<AchievementEntity>(database)
{
    //DB Fields
    var playerId: String = ""
    var achievementType: AchievementType = AchievementType.X01_BEST_FINISH
    var gameIdEarned = ""
    var achievementCounter = -1
    var achievementDetail = ""
    var dtAchieved: Timestamp = getSqlDateNow()

    //Other stuff
    var localGameIdEarned = -1L

    override fun getTableName() = EntityName.Achievement

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "AchievementType VARCHAR(255) NOT NULL, "
                + "GameIdEarned VARCHAR(36) NOT NULL, "
                + "AchievementCounter INT NOT NULL, "
                + "AchievementDetail VARCHAR(255) NOT NULL, "
                + "DtAchieved TIMESTAMP NOT NULL")
    }

    override fun includeInSync() = false

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "AchievementType"))
    }

    companion object
    {
        fun retrieveAchievements(playerId: String): MutableList<AchievementEntity>
        {
            val achievements = mutableListOf<AchievementEntity>()
            val dao = AchievementEntity()

            val sb = StringBuilder()
            sb.append("SELECT ${dao.getColumnsForSelectStatement("a")}, ")
            sb.append(" CASE WHEN g.LocalId IS NULL THEN -1 ELSE g.LocalId END AS LocalGameId")
            sb.append(" FROM Achievement a")
            sb.append(" LEFT OUTER JOIN Game g ON (a.GameIdEarned = g.RowId)")
            sb.append(" WHERE PlayerId = '$playerId'")

            mainDatabase.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val entity = dao.factoryFromResultSet(rs)
                    entity.localGameIdEarned = rs.getLong("LocalGameId")

                    achievements.add(entity)
                }
            }

            return achievements
        }


        fun retrieveAchievement(achievementType: AchievementType, playerId: String) =
            AchievementEntity().retrieveEntity("PlayerId = '$playerId' AND achievementType = '$achievementType'")

        /**
         * Methods for gameplay logic to update achievements
         */
        fun updateAchievement(achievementType: AchievementType, playerId: String, gameId: String, counter: Int)
        {
            val existingAchievement = retrieveAchievement(achievementType, playerId)

            if (existingAchievement == null)
            {
                factoryAndSave(achievementType, playerId, gameId, counter)

                triggerAchievementUnlock(-1, counter, achievementType, playerId, gameId)
            }
            else
            {
                val existingCounter = existingAchievement.achievementCounter

                val decreasing = getAchievementForType(achievementType)!!.isDecreasing()

                //Update the achievement if appropriate
                if (counter > existingCounter && !decreasing
                 || counter < existingCounter && decreasing)
                {
                    existingAchievement.achievementCounter = counter
                    existingAchievement.gameIdEarned = gameId
                    existingAchievement.dtAchieved = getSqlDateNow()
                    existingAchievement.saveToDatabase()

                    triggerAchievementUnlock(existingCounter, counter, achievementType, playerId, gameId)
                }
            }
        }

        fun insertAchievement(achievementType: AchievementType, playerId: String, gameId: String, detail: String = "", counter: Int = -1)
        {
            val sql = "SELECT COUNT(1) FROM Achievement WHERE PlayerId = '$playerId' AND AchievementType = '$achievementType'"
            val count = mainDatabase.executeQueryAggregate(sql)

            factoryAndSave(achievementType, playerId, gameId, counter, detail)
            triggerAchievementUnlock(count, count + 1, achievementType, playerId, gameId)
        }

        fun insertAchievementWithCounter(achievementType: AchievementType, playerId: String, gameId: String, detail: String, counter: Int)
        {
            val sql = "SELECT SUM(AchievementCounter) FROM Achievement WHERE PlayerId = '$playerId' AND AchievementType = '$achievementType'"
            val count = mainDatabase.executeQueryAggregate(sql)

            factoryAndSave(achievementType, playerId, gameId, counter, detail)
            triggerAchievementUnlock(count, count + counter, achievementType, playerId, gameId)
        }

        fun insertForUniqueCounter(achievementType: AchievementType, playerId: String, gameId: String, counter: Int, detail: String)
        {
            val whereSql = "PlayerId = '$playerId' AND AchievementType = '$achievementType'"

            val achievementRows = AchievementEntity().retrieveEntities(whereSql)
            val hits = achievementRows.map { it.achievementCounter }
            if (!hits.contains(counter))
            {
                val newRow = factoryAndSave(achievementType, playerId, gameId, counter, detail)
                val allRows = achievementRows + newRow

                triggerAchievementUnlock(achievementRows.size, achievementRows.size + 1, achievementType, playerId, gameId, allRows)
            }
        }

        private fun triggerAchievementUnlock(oldValue: Int, newValue: Int, achievementType: AchievementType, playerId: String, gameId: String, achievementRows: List<AchievementEntity>? = null)
        {
            val achievementTemplate = getAchievementForType(achievementType) ?: return
            achievementRows?.let { achievementTemplate.initialiseFromDb(achievementRows,null) }
            triggerAchievementUnlock(oldValue, newValue, achievementTemplate, playerId, gameId)
        }

        fun triggerAchievementUnlock(oldValue: Int, newValue: Int, achievementTemplate: AbstractAchievement, playerId: String, gameId: String)
        {
            //Work out if the threshold has changed
            achievementTemplate.attainedValue = oldValue
            val oldColor = achievementTemplate.getColor(false)

            achievementTemplate.attainedValue = newValue
            val newColor = achievementTemplate.getColor(false)

            if (oldColor !== newColor)
            {
                //Hooray we've done a thing!
                val scrn = ScreenCache.getDartsGameScreen(gameId)
                scrn?.achievementUnlocked(gameId, playerId, achievementTemplate)
            }
        }

        fun factory(achievementType: AchievementType,
                    playerId: String,
                    gameId: String,
                    counter: Int,
                    achievementDetail: String,
                    dtAchieved: Timestamp,
                    database: Database = mainDatabase): AchievementEntity
        {
            val ae = AchievementEntity(database)
            ae.assignRowId()
            ae.achievementType = achievementType
            ae.playerId = playerId
            ae.gameIdEarned = gameId
            ae.achievementCounter = counter
            ae.achievementDetail = achievementDetail
            ae.dtAchieved = dtAchieved
            return ae
        }

        fun factoryAndSave(achievementType: AchievementType,
                           playerId: String,
                           gameId: String,
                           counter: Int,
                           achievementDetail: String = "",
                           dtAchieved: Timestamp = getSqlDateNow(),
                           database: Database = mainDatabase): AchievementEntity
        {
            val ae = factory(achievementType, playerId, gameId, counter, achievementDetail, dtAchieved, database)
            ae.saveToDatabase()
            return ae
        }
    }
}
