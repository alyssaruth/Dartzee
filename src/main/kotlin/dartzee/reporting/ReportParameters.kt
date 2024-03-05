package dartzee.reporting

import dartzee.core.util.DateStatics
import dartzee.core.util.getEndOfTimeSqlString
import dartzee.core.util.getSqlString
import dartzee.db.SyncAuditEntity
import dartzee.utils.InjectedThings.mainDatabase

data class ReportParameters(val game: ReportParametersGame, val players: ReportParametersPlayers) {

    fun getExtraWhereSql(participantTempTable: String): String {
        val sb = StringBuilder()

        if (game.gameType != null) {
            sb.append(" AND g.GameType = '${game.gameType}'")
        }

        if (game.gameParams.isNotEmpty()) {
            sb.append(" AND g.GameParams = '${game.gameParams}'")
        }

        if (game.dtStartFrom != null) {
            sb.append(" AND g.DtCreation >= '")
            sb.append(game.dtStartFrom)
            sb.append("'")
        }

        if (game.dtStartTo != null) {
            sb.append(" AND g.DtCreation <= '")
            sb.append(game.dtStartTo)
            sb.append("'")
        }

        if (game.dtFinishFrom != null) {
            sb.append(" AND g.DtFinish >= '")
            sb.append(game.dtFinishFrom)
            sb.append("'")
        }

        if (game.dtFinishTo != null) {
            sb.append(" AND g.DtFinish <= '")
            sb.append(game.dtFinishTo)
            sb.append("'")
        }

        if (game.unfinishedOnly) {
            sb.append(" AND g.DtFinish = ")
            sb.append(getEndOfTimeSqlString())
        }

        if (game.partOfMatch == MatchFilter.GAMES_ONLY) {
            sb.append(" AND g.DartsMatchId = ''")
        } else if (game.partOfMatch == MatchFilter.MATCHES_ONLY) {
            sb.append(" AND g.DartsMatchId <> ''")
        }

        game.pendingChanges?.let { pendingChanges ->
            val dtLastSynced =
                SyncAuditEntity.getLastSyncData(mainDatabase)?.lastSynced
                    ?: DateStatics.START_OF_TIME
            if (pendingChanges) {
                sb.append(" AND g.DtLastUpdate > ${dtLastSynced.getSqlString()}")
            } else {
                sb.append(" AND g.DtLastUpdate <= ${dtLastSynced.getSqlString()}")
            }
        }

        val it = players.includedPlayers.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val player = entry.key
            val parms = entry.value

            sb.append(" AND EXISTS (")
            sb.append(" SELECT 1 FROM $participantTempTable z")
            sb.append(" WHERE z.PlayerId = '${player.rowId}'")
            sb.append(" AND z.GameId = g.RowId")

            val extraSql = parms.generateExtraWhereSql("z")
            sb.append(extraSql)

            sb.append(")")
        }

        for (player in players.excludedPlayers) {
            sb.append(" AND NOT EXISTS (")
            sb.append(" SELECT 1 FROM $participantTempTable z")
            sb.append(" WHERE z.PlayerId = '${player.rowId}'")
            sb.append(" AND z.GameId = g.RowId)")
        }

        if (players.excludeOnlyAi) {
            sb.append(" AND EXISTS (")
            sb.append(" SELECT 1 FROM $participantTempTable z, Player p")
            sb.append(" WHERE z.PlayerId = p.RowId")
            sb.append(" AND z.GameId = g.RowId")
            sb.append(" AND p.Strategy = '')")
        }

        return sb.toString()
    }
}

enum class MatchFilter {
    MATCHES_ONLY,
    GAMES_ONLY,
    BOTH
}
