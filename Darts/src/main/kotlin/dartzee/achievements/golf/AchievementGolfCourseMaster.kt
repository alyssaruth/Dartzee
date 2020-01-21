package dartzee.achievements.golf

import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import dartzee.achievements.AbstractAchievementRowPerGame
import dartzee.db.AchievementEntity
import dartzee.db.GAME_TYPE_GOLF
import dartzee.utils.DatabaseUtil
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementGolfCourseMaster : AbstractAchievementRowPerGame()
{
    override val name = "Course Master"
    override val desc = "Unique holes where a hole-in-one has been achieved"
    override val achievementRef = ACHIEVEMENT_REF_GOLF_COURSE_MASTER
    override val redThreshold = 1
    override val orangeThreshold = 3
    override val yellowThreshold = 6
    override val greenThreshold = 10
    override val blueThreshold = 14
    override val pinkThreshold = 18
    override val maxValue = 18
    override val gameType = GAME_TYPE_GOLF

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_GOLF_COURSE_MASTER

    override fun getBreakdownColumns() = listOf("Hole", "Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.achievementDetail.toInt(), a.localGameIdEarned, a.dtLastUpdate)
    override fun isUnbounded() = false

    override fun populateForConversion(playerIds: String)
    {
        val tempTable = DatabaseUtil.createTempTable("PlayerHolesInOne", "PlayerId VARCHAR(36), Score INT, GameId VARCHAR(36), DtAchieved TIMESTAMP")
                ?: return

        var sb = StringBuilder()

        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, d.Score, g.RowId, d.DtCreation")
        sb.append(" FROM Dart d, Participant pt, Game g")
        sb.append(" WHERE d.SegmentType = $SEGMENT_TYPE_DOUBLE")
        sb.append(" AND d.RoundNumber = d.Score")
        sb.append(" AND d.ParticipantId = pt.RowId")
        sb.append(" AND d.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_GOLF")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        if (!DatabaseUtil.executeUpdate("" + sb))
            return

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, Score, GameId, DtAchieved")
        sb.append(" FROM $tempTable zz1")
        sb.append(" WHERE NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM $tempTable zz2")
        sb.append("     WHERE zz1.PlayerId = zz2.PlayerId")
        sb.append("     AND zz1.Score = zz2.Score")
        sb.append("     AND zz2.DtAchieved < zz1.DtAchieved")
        sb.append(")")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val hole = rs.getInt("Score")
                val gameId = rs.getString("GameId")
                val dtAchieved = rs.getTimestamp("DtAchieved")

                AchievementEntity.factoryAndSave(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, playerId, gameId, -1, "$hole", dtAchieved)
            }
        }

        DatabaseUtil.dropTable(tempTable)
    }
}