package dartzee.achievements.dartzee

import dartzee.achievements.*
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementDartzeeFlawless: AbstractMultiRowAchievement()
{
    override val name = "Flawless"
    override val desc = "Games where all rules were passed (at least 5 rules)"
    override val achievementType = AchievementType.DARTZEE_FLAWLESS
    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 5
    override val greenThreshold = 10
    override val blueThreshold = 25
    override val pinkThreshold = 50
    override val maxValue = 50
    override val gameType = GameType.DARTZEE

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_DARTZEE_FLAWLESS

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val dartzeeGames = buildQualifyingDartzeeGamesTable(database) ?: return

        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, pt.DtFinished AS DtAchieved, zz.GameId, pt.FinalScore, zz.TemplateName")
        sb.append(" FROM ${EntityName.Participant} pt, $dartzeeGames zz")
        sb.append(" WHERE pt.GameId = zz.GameId")
        sb.append(" AND pt.FinalScore > -1")
        appendPlayerSql(sb, playerIds)
        sb.append(" AND NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM ${EntityName.DartzeeRoundResult} drr")
        sb.append("     WHERE drr.ParticipantId = pt.RowId")
        sb.append("     AND drr.success = false")
        sb.append(" )")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, achievementCounterFn = { rs.getInt("FinalScore") }, achievementDetailFn = { rs.getString("TemplateName")})
        }
    }


    override fun getBreakdownColumns() = listOf("Game", "Score", "Template", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf<Any>(a.localGameIdEarned, a.achievementCounter, a.achievementDetail, a.dtAchieved)
}