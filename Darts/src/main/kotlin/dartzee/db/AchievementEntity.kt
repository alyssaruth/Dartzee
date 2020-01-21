package dartzee.db

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.getAchievementForRef
import dartzee.screen.ScreenCache
import dartzee.utils.DatabaseUtil
import dartzee.core.util.getSqlDateNow
import java.sql.Timestamp

/**
 * Entity to record a particular that a particular achievement has been earned by a player.
 *
 * Points at a GameId if applicable, and uses DtCreation as the "date unlocked".
 */
class AchievementEntity : AbstractEntity<AchievementEntity>()
{
    //DB Fields
    var playerId: String = ""
    var achievementRef = -1
    var gameIdEarned = ""
    var achievementCounter = -1
    var achievementDetail = ""

    //Other stuff
    var localGameIdEarned = -1L

    override fun getTableName() = "Achievement"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "AchievementRef INT NOT NULL, "
                + "GameIdEarned VARCHAR(36) NOT NULL, "
                + "AchievementCounter INT NOT NULL, "
                + "AchievementDetail VARCHAR(255) NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "AchievementRef"))
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

            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val entity = dao.factoryFromResultSet(rs)
                    entity.localGameIdEarned = rs.getLong("LocalGameId")

                    achievements.add(entity)
                }
            }

            return achievements
        }


        fun retrieveAchievement(achievementRef: Int, playerId: String): AchievementEntity?
        {
            return AchievementEntity().retrieveEntity("PlayerId = '$playerId' AND AchievementRef = $achievementRef")
        }

        /**
         * Methods for gameplay logic to update achievements
         */
        fun updateAchievement(achievementRef: Int, playerId: String, gameId: String, counter: Int)
        {
            val existingAchievement = retrieveAchievement(achievementRef, playerId)

            if (existingAchievement == null)
            {
                factoryAndSave(achievementRef, playerId, gameId, counter)

                triggerAchievementUnlock(-1, counter, achievementRef, playerId, gameId)
            }
            else
            {
                val existingCounter = existingAchievement.achievementCounter

                val decreasing = getAchievementForRef(achievementRef)!!.isDecreasing()

                //Update the achievement if appropriate
                if (counter > existingCounter && !decreasing
                 || counter < existingCounter && decreasing)
                {
                    existingAchievement.achievementCounter = counter
                    existingAchievement.gameIdEarned = gameId
                    existingAchievement.saveToDatabase()

                    triggerAchievementUnlock(existingCounter, counter, achievementRef, playerId, gameId)
                }
            }
        }

        fun incrementAchievement(achievementRef: Int, playerId: String, gameId: String, amountBy: Int = 1)
        {
            val existingAchievement = retrieveAchievement(achievementRef, playerId)

            if (existingAchievement == null)
            {
                factoryAndSave(achievementRef, playerId, "", amountBy)

                triggerAchievementUnlock(-1, amountBy, achievementRef, playerId, gameId)
            }
            else
            {
                val existingCount = existingAchievement.achievementCounter
                existingAchievement.achievementCounter = existingCount + amountBy
                existingAchievement.saveToDatabase()

                triggerAchievementUnlock(existingCount, existingCount + amountBy, achievementRef, playerId, gameId)
            }
        }

        fun insertAchievement(achievementRef: Int, playerId: String, gameId: String, detail: String = "")
        {
            val sql = "SELECT COUNT(1) FROM Achievement WHERE PlayerId = '$playerId' AND AchievementRef = $achievementRef"
            val count = DatabaseUtil.executeQueryAggregate(sql)

            factoryAndSave(achievementRef, playerId, gameId, -1, detail)
            triggerAchievementUnlock(count, count + 1, achievementRef, playerId, gameId)
        }

        private fun triggerAchievementUnlock(oldValue: Int, newValue: Int, achievementRef: Int, playerId: String, gameId: String)
        {
            val achievementTemplate = getAchievementForRef(achievementRef)
            triggerAchievementUnlock(oldValue, newValue, achievementTemplate!!, playerId, gameId)
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

        fun factoryAndSave(achievementRef: Int, playerId: String, gameId: String, counter: Int,
                           achievementDetail: String = "", dtLastUpdate: Timestamp = getSqlDateNow()): AchievementEntity
        {
            val ae = AchievementEntity()
            ae.assignRowId()
            ae.achievementRef = achievementRef
            ae.playerId = playerId
            ae.gameIdEarned = gameId
            ae.achievementCounter = counter
            ae.achievementDetail = achievementDetail
            ae.saveToDatabase(dtLastUpdate)

            return ae
        }
    }
}
