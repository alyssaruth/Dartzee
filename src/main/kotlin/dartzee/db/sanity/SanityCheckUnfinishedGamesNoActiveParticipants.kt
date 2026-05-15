package dartzee.db.sanity

import dartzee.core.util.getEndOfTimeSqlString
import dartzee.db.GameEntity

class SanityCheckUnfinishedGamesNoActiveParticipants : ISanityCheck {
    override fun runCheck(): List<SanityCheckResult> {
        val sb = StringBuilder()
        sb.append("DtFinish = ${getEndOfTimeSqlString()}")
        sb.append(" AND NOT EXISTS ")
        sb.append(" (")
        sb.append(" 	SELECT 1")
        sb.append(" 	FROM Participant pt")
        sb.append(" 	WHERE pt.GameId = g.RowId")
        sb.append(" 	AND pt.DtFinished = ${getEndOfTimeSqlString()}")
        sb.append(" )")

        val whereSql = sb.toString()
        val games = GameEntity().retrieveEntities(whereSql, "g")
        if (games.isNotEmpty()) {
            return listOf(
                SanityCheckResultEntities(
                    games,
                    "DtFinish is unset but there are no active players",
                )
            )
        }

        return listOf()
    }
}
