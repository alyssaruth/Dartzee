package dartzee.achievements.rtc

import dartzee.achievements.AchievementType
import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.AchievementEntity
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementClockBruceyBonuses : AbstractMultiRowAchievement()
{
    override val name = "Didn't he do well!?"
    override val desc = "Total number of 'Brucey Bonuses' executed in Round the Clock"
    override val achievementType = AchievementType.CLOCK_BRUCEY_BONUSES
    override val gameType = GameType.ROUND_THE_CLOCK

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = 100

    override fun isUnbounded() = true

    override fun getBreakdownColumns() = listOf("Game", "Round", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf<Any>(a.localGameIdEarned, a.achievementDetail.toInt(), a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, pt.GameId, drt.RoundNumber, drt.DtCreation AS DtAchieved")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.ROUND_THE_CLOCK}'")
        sb.append(" AND drt.Ordinal = 4")
        sb.append(" AND drt.Score = drt.StartingScore")
        sb.append(" AND (")
        sb.append("        (g.GameParams LIKE '%${ClockType.Standard}%' AND drt.Multiplier > 0)")
        sb.append("     OR (g.GameParams LIKE '%${ClockType.Doubles}%' AND drt.Multiplier = 2)")
        sb.append("     OR (g.GameParams LIKE '%${ClockType.Trebles}%' AND drt.Multiplier = 3)")
        sb.append(" )")
        appendPlayerSql(sb, playerIds)

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, achievementDetailFn = { rs.getInt("RoundNumber").toString() })
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES
}