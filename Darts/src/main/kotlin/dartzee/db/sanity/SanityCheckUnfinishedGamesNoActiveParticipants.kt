package dartzee.db.sanity

import dartzee.db.GameEntity
import dartzee.core.util.getEndOfTimeSqlString

class SanityCheckUnfinishedGamesNoActiveParticipants: AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult>
    {
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
        if (games.size > 0)
        {
            return listOf(SanityCheckResultEntitiesSimple(games, "Unfinished games without active players"))
        }

        return listOf()
    }
}