package dartzee.achievements.golf

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.`object`.SegmentType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementGolfOneHitWonder : AbstractAchievement()
{
    override val name = "One Hit Wonder"
    override val desc = "Most holes-in-one in a single game of Golf"
    override val achievementType = AchievementType.GOLF_ONE_HIT_WONDER
    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 4
    override val greenThreshold = 6
    override val blueThreshold = 9
    override val pinkThreshold = 12
    override val maxValue = 18
    override val gameType = GameType.GOLF
    override val allowedForTeams = true

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val tempTable = database.createTempTable("GameHolesInOne", "PlayerId VARCHAR(36), GameId VARCHAR(36), DtAchieved TIMESTAMP, HoleInOneCount INT")
            ?: return

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, g.RowId, MAX(d.DtCreation), COUNT(1)")
        sb.append(" FROM ${EntityName.Dart} d, ${EntityName.Participant} pt, ${EntityName.Game} g")
        sb.append(" WHERE d.ParticipantId = pt.RowId")
        sb.append(" AND d.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.GOLF}'")
        sb.append(" AND d.Score = d.RoundNumber")
        sb.append(" AND d.SegmentType = '${SegmentType.DOUBLE}'")
        appendPlayerSql(sb, playerIds)
        sb.append(" GROUP BY pt.PlayerId, g.RowId")

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, HoleInOneCount, GameId, DtAchieved")
        sb.append(" FROM $tempTable zz1")
        sb.append(" WHERE NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM $tempTable zz2")
        sb.append("     WHERE zz1.PlayerId = zz2.PlayerId")
        sb.append("     AND (zz2.HoleInOneCount > zz1.HoleInOneCount OR (zz2.HoleInOneCount = zz1.HoleInOneCount AND zz2.DtAchieved < zz1.DtAchieved))")
        sb.append(")")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, achievementCounterFn = { rs.getInt("HoleInOneCount") })
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_GOLF_ONE_HIT_WONDER
}