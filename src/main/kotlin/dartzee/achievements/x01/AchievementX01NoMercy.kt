package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.LAST_ROUND_FROM_PARTICIPANT
import dartzee.achievements.LAST_ROUND_FROM_TEAM
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementX01NoMercy : AbstractMultiRowAchievement() {
    override val name = "No Mercy"
    override val desc = "Finishes from 3, 5, 7 or 9 in X01"
    override val achievementType = AchievementType.X01_NO_MERCY
    override val gameType = GameType.X01
    override val allowedForTeams = true

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val sb = StringBuilder()
        sb.append(" SELECT drt.StartingScore, pt.PlayerId, pt.GameId, drt.DtCreation AS DtAchieved")
        sb.append(" FROM Game g, Dart drt, Participant pt")
        sb.append(" LEFT OUTER JOIN Team t ON (pt.TeamId = t.RowId)")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.X01}'")
        sb.append(" AND (pt.FinalScore > -1 OR t.FinalScore > -1)")
        sb.append(
            " AND ($LAST_ROUND_FROM_PARTICIPANT = drt.RoundNumber OR $LAST_ROUND_FROM_TEAM = drt.RoundNumber)"
        )
        sb.append(" AND pt.RowId = drt.ParticipantId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND drt.Ordinal = 1")
        sb.append(" AND drt.StartingScore IN (3, 5, 7, 9)")
        appendPlayerSql(sb, playerIds)

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(
                rs,
                database,
                achievementType,
                achievementDetailFn = { rs.getInt("StartingScore").toString() }
            )
        }
    }

    override fun getBreakdownColumns() = listOf("Checkout", "Game", "Date Achieved")

    override fun getBreakdownRow(a: AchievementEntity) =
        arrayOf<Any>(a.achievementDetail, a.localGameIdEarned, a.dtAchieved)

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_X01_NO_MERCY
}
