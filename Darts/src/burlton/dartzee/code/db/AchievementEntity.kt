package burlton.dartzee.code.db

import burlton.core.code.obj.HandyArrayList
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.getAchievementForRef
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.getSqlDateNow
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

/**
 * Entity to record a particular that a particular achievement has been earned by a player.
 *
 * Points at a GameId if applicable, and uses DtCreation as the "date unlocked".
 */
class AchievementEntity : AbstractEntity<AchievementEntity>()
{
    //DB Fields
    var playerId: Long = -1
    var achievementRef = -1
    var gameIdEarned: Long = -1
    var achievementCounter = -1
    var achievementDetail = ""

    override fun getTableName(): String
    {
        return "Achievement"
    }

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId INT NOT NULL, "
                + "AchievementRef INT NOT NULL, "
                + "GameIdEarned INT NOT NULL, "
                + "AchievementCounter INT NOT NULL, "
                + "AchievementDetail VARCHAR(255) NOT NULL")
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: AchievementEntity, rs: ResultSet)
    {
        entity.playerId = rs.getLong("PlayerId")
        entity.achievementRef = rs.getInt("AchievementRef")
        entity.gameIdEarned = rs.getLong("GameIdEarned")
        entity.achievementCounter = rs.getInt("AchievementCounter")
        entity.achievementDetail = rs.getString("AchievementDetail")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement

        statementStr = writeLong(statement, i++, playerId, statementStr)
        statementStr = writeInt(statement, i++, achievementRef, statementStr)
        statementStr = writeLong(statement, i++, gameIdEarned, statementStr)
        statementStr = writeInt(statement, i++, achievementCounter, statementStr)
        statementStr = writeString(statement, i, achievementDetail, statementStr)

        return statementStr
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<MutableList<String>>)
    {
        val ix = HandyArrayList.factoryAdd("PlayerId", "AchievementRef")

        indexes.add(ix)
    }

    companion object
    {

        @JvmStatic fun retrieveAchievement(achievementRef: Int, playerId: Long): AchievementEntity?
        {
            return AchievementEntity().retrieveEntity("PlayerId = $playerId AND AchievementRef = $achievementRef")
        }

        /**
         * Methods for gameplay logic to update achievements
         */
        @JvmStatic fun updateAchievement(achievementRef: Int, playerId: Long, gameId: Long, counter: Int)
        {
            val existingAchievement = retrieveAchievement(achievementRef, playerId)

            if (existingAchievement == null)
            {
                AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, counter)

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

        @JvmStatic fun incrementAchievement(achievementRef: Int, playerId: Long, gameId: Long, amountBy: Int = 1)
        {
            val existingAchievement = retrieveAchievement(achievementRef, playerId)

            if (existingAchievement == null)
            {
                AchievementEntity.factoryAndSave(achievementRef, playerId, -1, amountBy)

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

        fun insertAchievement(achievementRef: Int, playerId: Long, gameId: Long, detail: String = "")
        {
            val sql = "SELECT COUNT(1) FROM Achievement WHERE PlayerId = $playerId AND AchievementRef = $achievementRef"
            val count = DatabaseUtil.executeQueryAggregate(sql)

            factoryAndSave(achievementRef, playerId, gameId, -1, detail)
            triggerAchievementUnlock(count, count + 1, achievementRef, playerId, gameId)
        }

        private fun triggerAchievementUnlock(oldValue: Int, newValue: Int, achievementRef: Int, playerId: Long, gameId: Long)
        {
            val achievementTemplate = getAchievementForRef(achievementRef)
            triggerAchievementUnlock(oldValue, newValue, achievementTemplate!!, playerId, gameId)
        }

        fun triggerAchievementUnlock(oldValue: Int, newValue: Int, achievementTemplate: AbstractAchievement, playerId: Long, gameId: Long)
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
                scrn.achievementUnlocked(gameId, playerId, achievementTemplate)
            }
        }

        @JvmOverloads
        @JvmStatic fun factoryAndSave(achievementRef: Int, playerId: Long, gameId: Long, counter: Int,
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
