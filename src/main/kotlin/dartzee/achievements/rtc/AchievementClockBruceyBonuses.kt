package dartzee.achievements.rtc

import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.appendPlayerSql
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementClockBruceyBonuses : AbstractMultiRowAchievement()
{
    override val name = "Didn't he do well!?"
    override val desc = "Total number of 'Brucey Bonuses' executed in Round the Clock"
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
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
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.localGameIdEarned, a.achievementDetail.toInt(), a.dtAchieved)

    override fun populateForConversion(players: List<PlayerEntity>, database: Database)
    {
        val sb = StringBuilder()
        sb.append(" SELECT pt.PlayerId, pt.GameId, drt.RoundNumber, drt.DtCreation AS DtLastUpdate")
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
        appendPlayerSql(sb, players)

        database.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val roundNumber = rs.getInt("RoundNumber")
                val dtLastUpdate = rs.getTimestamp("DtLastUpdate")

                AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, -1, "$roundNumber", dtLastUpdate, database)
            }
        }
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES
}