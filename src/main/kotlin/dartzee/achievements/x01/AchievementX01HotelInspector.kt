package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR
import dartzee.achievements.AbstractAchievementRowPerGame
import dartzee.achievements.getNotBustSql
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.InjectedThings.logger
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR
import dartzee.utils.TOTAL_ROUND_SCORE_SQL_STR
import java.net.URL
import java.sql.SQLException

class AchievementX01HotelInspector : AbstractAchievementRowPerGame()
{
    override val name = "Hotel Inspector"
    override val desc = "Number of distinct ways the player has scored 26 (\"Bed and Breakfast\")"
    override val achievementRef = ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR
    override val gameType = GameType.X01

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 15
    override val blueThreshold = 20
    override val pinkThreshold = 26
    override val maxValue = 26

    override fun getIconURL(): URL = URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR

    override fun getBreakdownColumns() = listOf("Method", "Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.achievementDetail, a.localGameIdEarned, a.dtLastUpdate)

    override fun populateForConversion(playerIds: String)
    {
        val tempTable = mainDatabase.createTempTable("BurltonConstants", "PlayerId VARCHAR(36), ParticipantId VARCHAR(36), GameId VARCHAR(36), Ordinal INT, Score INT, Multiplier INT, RoundNumber INT, DtCreation TIMESTAMP")
        tempTable ?: return

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, pt.RowId, pt.GameId, d.Ordinal, d.Score, d.Multiplier, d.RoundNumber, d.DtCreation")
        sb.append(" FROM Dart d, Dart drtFirst, Dart drtSecond, Dart drtLast, Participant pt, Game g")
        sb.append(" WHERE drtFirst.ParticipantId = pt.RowId")
        sb.append(" AND drtFirst.PlayerId = pt.PlayerId")
        sb.append(" AND drtSecond.ParticipantId = pt.RowId")
        sb.append(" AND drtSecond.PlayerId = pt.PlayerId")
        sb.append(" AND drtLast.ParticipantId = pt.RowId")
        sb.append(" AND drtLast.PlayerId = pt.PlayerId")
        sb.append(" AND d.RoundNumber = drtFirst.RoundNumber")
        sb.append(" AND drtFirst.RoundNumber = drtSecond.RoundNumber")
        sb.append(" AND drtSecond.RoundNumber = drtLast.RoundNumber")
        sb.append(" AND d.ParticipantId = pt.RowId")
        sb.append(" AND d.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.X01}'")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND drtSecond.Ordinal = 2")
        sb.append(" AND drtLast.Ordinal = 3")
        sb.append(" AND drtFirst.Multiplier > 0")
        sb.append(" AND drtSecond.Multiplier > 0")
        sb.append(" AND drtLast.Multiplier > 0")
        sb.append(" AND $TOTAL_ROUND_SCORE_SQL_STR = 26")
        sb.append(" AND ${getNotBustSql()}")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        if (!mainDatabase.executeUpdate("" + sb))
        {
            mainDatabase.dropTable(tempTable)
            return
        }

        mainDatabase.executeUpdate("CREATE INDEX ${tempTable}_PlayerId_ParticipantId_RoundNumber ON $tempTable(PlayerId, ParticipantId, RoundNumber)")
        val tempTableTwo = mainDatabase.createTempTable("BurltonConstantsFlat", "PlayerId VARCHAR(36), GameId VARCHAR(36), DtAchieved TIMESTAMP, Method VARCHAR(100)")

        sb = StringBuilder()
        sb.append(" INSERT INTO $tempTableTwo")
        sb.append(" SELECT highestDart.PlayerId, highestDart.GameId, highestDart.DtCreation, ${getThreeDartMethodSqlStr()} AS Method")
        sb.append(" FROM $tempTable highestDart, $tempTable mediumDart, $tempTable lowestDart")
        sb.append(" WHERE highestDart.ParticipantId = mediumDart.ParticipantId")
        sb.append(" AND highestDart.PlayerId = mediumDart.PlayerId")
        sb.append(" AND highestDart.RoundNumber = mediumDart.RoundNumber")
        sb.append(" AND mediumDart.ParticipantId = lowestDart.ParticipantId")
        sb.append(" AND mediumDart.PlayerId = lowestDart.PlayerId")
        sb.append(" AND mediumDart.RoundNumber = lowestDart.RoundNumber")
        sb.append(" AND (${getDartHigherThanSql("highestDart", "mediumDart")})")
        sb.append(" AND (${getDartHigherThanSql("mediumDart", "lowestDart")})")
        sb.append(" GROUP BY highestDart.PlayerId, highestDart.GameId, highestDart.DtCreation, ${getThreeDartMethodSqlStr()}")

        if (!mainDatabase.executeUpdate("" + sb))
        {
            mainDatabase.dropTable(tempTable)
            mainDatabase.dropTable(tempTableTwo)
            return
        }

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, GameId, DtAchieved, Method")
        sb.append(" FROM $tempTableTwo zz")
        sb.append(" WHERE NOT EXISTS")
        sb.append(" (")
        sb.append("     SELECT 1")
        sb.append("     FROM $tempTableTwo zz2")
        sb.append("     WHERE zz.PlayerId = zz2.PlayerId")
        sb.append("     AND zz.Method = zz2.Method")
        sb.append("     AND zz2.DtAchieved < zz.DtAchieved")
        sb.append(" )")

        try
        {
            val rs = mainDatabase.executeQuery(sb)
            rs.use{
                while (rs.next())
                {
                    val playerId = rs.getString("PlayerId")
                    val gameId = rs.getString("GameId")
                    val method = rs.getString("Method")
                    val dtAchieved = rs.getTimestamp("DtAchieved")

                    AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, -1, method, dtAchieved)
                }
            }
        }
        catch (sqle: SQLException)
        {
            logger.logSqlException("" + sb, "" + sb, sqle)
        }
        finally
        {
            mainDatabase.dropTable(tempTable)
            mainDatabase.dropTable(tempTableTwo)
        }
    }

    private fun getDartHigherThanSql(hAlias: String, lAlias: String): String
    {
        val sb = StringBuilder()

        sb.append("($hAlias.Score * $hAlias.Multiplier) > ($lAlias.Score * $lAlias.Multiplier)") //Higher score outright
        sb.append(" OR (($hAlias.Score * $hAlias.Multiplier) = ($lAlias.Score * $lAlias.Multiplier) AND $hAlias.Multiplier > $lAlias.Multiplier)")
        sb.append(" OR ($hAlias.Score = $lAlias.Score AND $hAlias.Multiplier = $lAlias.Multiplier AND $hAlias.Ordinal > $lAlias.Ordinal)")

        return sb.toString()
    }

    private fun getThreeDartMethodSqlStr(): String
    {
        return "${getDartStrSql("highestDart")} || ', ' || ${getDartStrSql("mediumDart")} || ', ' || ${getDartStrSql("lowestDart")}"
    }

    private fun getDartStrSql(alias: String): String
    {
        return "${getDartMultiplierStrSql(alias)} || ${getDartScoreStrSql(alias)}"
    }
    private fun getDartMultiplierStrSql(alias: String): String
    {
        return "CASE WHEN $alias.Multiplier = 3 THEN 'T' WHEN $alias.Multiplier = 2 THEN 'D' ELSE '' END"
    }
    private fun getDartScoreStrSql(alias: String): String
    {
        return "RTRIM(CAST(CASE WHEN $alias.Multiplier = 0 THEN 0 ELSE $alias.Score END AS CHAR(5)))"
    }
}
