package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementDartzeeHalved: AbstractAchievement()
{
    override val name = "Halved"
    override val desc = "Biggest loss of points in a single round of Dartzee"
    override val achievementType = AchievementType.DARTZEE_HALVED
    override val redThreshold = 20
    override val orangeThreshold = 40
    override val yellowThreshold = 75
    override val greenThreshold = 100
    override val blueThreshold = 150
    override val pinkThreshold = 200
    override val maxValue = 200
    override val gameType = GameType.DARTZEE

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val sb = StringBuilder()
        sb.append(" SELECT drr.DtCreation as DtAchieved, drr.Score * -1 AS Score, pt.PlayerId, pt.GameId")
        sb.append(" FROM ${EntityName.DartzeeRoundResult} drr, ${EntityName.Participant} pt, ${EntityName.Game} g")
        sb.append(" WHERE drr.Success = false")
        sb.append(" AND drr.ParticipantId = pt.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.DARTZEE}'")
        appendPlayerSql(sb, playerIds)
        sb.append(" ORDER BY drr.Score, drr.DtCreation")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, oneRowPerPlayer = true, achievementCounterFn = { rs.getInt("Score") })
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_DARTZEE_HALVED
    override fun isUnbounded() = true
}