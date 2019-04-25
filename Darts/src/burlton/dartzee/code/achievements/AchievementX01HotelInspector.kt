package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.stats.overall.TOTAL_ROUND_SCORE_SQL_STR
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache.URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR
import burlton.desktopcore.code.util.TableUtil
import java.net.URL
import java.sql.SQLException

class AchievementX01HotelInspector : AbstractAchievement()
{
    override val name = "Hotel Inspector"
    override val desc = "Number of distinct ways the player has scored 26 (\"Bed and Breakfast\")"
    override val achievementRef = ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 15
    override val blueThreshold = 20
    override val pinkThreshold = 26
    override val maxValue = 26

    override fun populateForConversion(playerIds: String)
    {
        val tempTable = DatabaseUtil.createTempTable("BurltonConstants", "PlayerId VARCHAR(36), GameId VARCHAR(36), Ordinal INT, Score INT, Multiplier INT, RoundId VARCHAR(36), DtCreation TIMESTAMP")
        tempTable ?: return

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, pt.GameId, d.Ordinal, d.Score, d.Multiplier, d.RoundId, d.DtCreation")
        sb.append(" FROM Dart d, Dart drtFirst, Dart drtSecond, Dart drtLast, Round rnd, Participant pt, Game g")
        sb.append(" WHERE drtFirst.RoundId = rnd.RowId")
        sb.append(" AND drtSecond.RoundId = rnd.RowId")
        sb.append(" AND drtLast.RoundId = rnd.RowId")
        sb.append(" AND d.RoundId = rnd.RowId")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_X01")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND drtSecond.Ordinal = 2")
        sb.append(" AND drtLast.Ordinal = 3")
        sb.append(" AND drtFirst.Multiplier > 0")
        sb.append(" AND drtSecond.Multiplier > 0")
        sb.append(" AND drtLast.Multiplier > 0")
        sb.append(" AND $TOTAL_ROUND_SCORE_SQL_STR = 26")
        sb.append(getNotBustSql())
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        if (!DatabaseUtil.executeUpdate("" + sb))
        {
            DatabaseUtil.dropTable(tempTable)
            return
        }

        sb = StringBuilder()
        sb.append(" SELECT highestDart.PlayerId, MIN(highestDart.GameId) AS GameId, MIN(highestDart.DtCreation) AS DtAchieved, ${getThreeDartMethodSqlStr()} AS Method")
        sb.append(" FROM $tempTable highestDart, $tempTable mediumDart, $tempTable lowestDart")
        sb.append(" WHERE highestDart.RoundId = mediumDart.RoundId")
        sb.append(" AND mediumDart.RoundId = lowestDart.RoundId")
        sb.append(" AND (${getDartHigherThanSql("highestDart", "mediumDart")})")
        sb.append(" AND (${getDartHigherThanSql("mediumDart", "lowestDart")})")
        sb.append(" GROUP BY highestDart.PlayerId, ${getThreeDartMethodSqlStr()}")

        try
        {
            val rs = DatabaseUtil.executeQuery(sb)
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
            Debug.logSqlException("" + sb, sqle)
        }
        finally
        {
            DatabaseUtil.dropTable(tempTable)
        }
    }

    override fun initialiseFromDb(achievementRows: MutableList<AchievementEntity>, player: PlayerEntity?)
    {
        this.player = player

        attainedValue = achievementRows.size

        if (!achievementRows.isEmpty())
        {
            val sortedRows = achievementRows.sortedBy {it.dtLastUpdate}
            val last = sortedRows.last()

            dtLatestUpdate = last.dtLastUpdate

            val tm = TableUtil.DefaultModel()
            tm.addColumn("Method")
            tm.addColumn("Game")
            tm.addColumn("Date Achieved")

            sortedRows.forEach{
                tm.addRow(arrayOf(it.achievementDetail, it.localGameIdEarned, it.dtLastUpdate))
            }

            tmBreakdown = tm
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

    override fun getIconURL(): URL = URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR

    override fun isUnbounded(): Boolean
    {
        return true
    }
}
