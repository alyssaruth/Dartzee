package dartzee.achievements

import dartzee.utils.Database
import dartzee.utils.doesHighestWin

abstract class AbstractAchievementBestGame : AbstractAchievement()
{
    abstract val gameParams: String
    override val allowedForTeams = false

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, g.RowId AS GameId, pt.FinalScore, pt.DtFinished AS DtAchieved")
        sb.append(" FROM Participant pt, Game g")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '$gameType'")
        sb.append(" AND g.GameParams = '$gameParams'")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND pt.TeamId = ''")
        appendPlayerSql(sb, playerIds)
        sb.append(" AND NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM Participant pt2, Game g2")
        sb.append("     WHERE pt2.GameId = g2.RowId")
        sb.append("     AND g2.GameType = g.GameType")
        sb.append("     AND g2.GameParams = g.GameParams")
        sb.append("     AND pt2.PlayerId = pt.PlayerId")
        sb.append("     AND pt2.FinalScore > -1")
        sb.append("     AND (pt2.FinalScore < pt.FinalScore OR (pt2.FinalScore = pt.FinalScore AND pt2.DtFinished < pt.DtFinished))")
        sb.append(")")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, achievementCounterFn = { rs.getInt("FinalScore") })
        }
    }

    override fun isDecreasing() = !doesHighestWin(gameType)
}