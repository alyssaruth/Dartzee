package dartzee.reporting

import dartzee.core.util.getEndOfTimeSqlString
import dartzee.db.GameType
import dartzee.db.PlayerEntity
import java.sql.Timestamp
import java.util.*

class ReportParameters
{
    var gameType: GameType? = null
    var gameParams = ""
    var unfinishedOnly = false
    var dtStartFrom: Timestamp? = null
    var dtStartTo: Timestamp? = null
    var dtFinishFrom: Timestamp? = null
    var dtFinishTo: Timestamp? = null
    var hmIncludedPlayerToParms = mutableMapOf<PlayerEntity, IncludedPlayerParameters>()
    var excludedPlayers: List<PlayerEntity> = ArrayList()
    private var partOfMatch = MATCH_FILTER_BOTH

    fun getExtraWhereSql(): String
    {
        val sb = StringBuilder()

        if (gameType != null)
        {
            sb.append(" AND g.GameType = '$gameType'")
        }

        if (!gameParams.isEmpty())
        {
            sb.append(" AND g.GameParams = '$gameParams'")
        }

        if (dtStartFrom != null)
        {
            sb.append(" AND g.DtCreation >= '")
            sb.append(dtStartFrom)
            sb.append("'")
        }

        if (dtStartTo != null)
        {
            sb.append(" AND g.DtCreation <= '")
            sb.append(dtStartTo)
            sb.append("'")
        }

        if (dtFinishFrom != null)
        {
            sb.append(" AND g.DtFinish >= '")
            sb.append(dtFinishFrom)
            sb.append("'")
        }

        if (dtFinishTo != null)
        {
            sb.append(" AND g.DtFinish <= '")
            sb.append(dtFinishTo)
            sb.append("'")
        }

        if (unfinishedOnly)
        {
            sb.append(" AND g.DtFinish = ")
            sb.append(getEndOfTimeSqlString())
        }

        if (partOfMatch == MATCH_FILTER_GAMES_ONLY)
        {
            sb.append(" AND g.DartsMatchId = ''")
        }
        else if (partOfMatch == MATCH_FILTER_MATCHES_ONLY)
        {
            sb.append(" AND g.DartsMatchId <> ''")
        }

        val it = hmIncludedPlayerToParms.entries.iterator()
        while (it.hasNext())
        {
            val entry = it.next()
            val player = entry.key
            val parms = entry.value

            sb.append(" AND EXISTS (")
            sb.append(" SELECT 1 FROM Participant z")
            sb.append(" WHERE z.PlayerId = '${player.rowId}'")
            sb.append(" AND z.GameId = g.RowId")

            val extraSql = parms.generateExtraWhereSql("z")
            sb.append(extraSql)

            sb.append(")")
        }

        for (player in excludedPlayers)
        {
            sb.append(" AND NOT EXISTS (")
            sb.append(" SELECT 1 FROM Participant z")
            sb.append(" WHERE z.PlayerId = '${player.rowId}'")
            sb.append(" AND z.GameId = g.RowId)")
        }

        return sb.toString()
    }

    override fun toString(): String
    {
        return "[$gameType, $gameParams, $dtStartFrom, $dtStartTo, $dtFinishFrom, $dtFinishTo]"
    }

    fun setEnforceMatch(matches: Boolean)
    {
        partOfMatch = if (matches) MATCH_FILTER_MATCHES_ONLY else MATCH_FILTER_GAMES_ONLY
    }

    companion object
    {
        private const val MATCH_FILTER_MATCHES_ONLY = 0
        private const val MATCH_FILTER_GAMES_ONLY = 1
        private const val MATCH_FILTER_BOTH = 2
    }
}
