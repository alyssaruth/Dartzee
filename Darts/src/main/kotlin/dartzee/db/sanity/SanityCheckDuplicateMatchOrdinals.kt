package dartzee.db.sanity

import dartzee.db.GameEntity

class SanityCheckDuplicateMatchOrdinals: AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult>
    {
        val sb = StringBuilder()
        sb.append(" g.MatchOrdinal > -1")
        sb.append(" AND EXISTS (")
        sb.append(" SELECT 1")
        sb.append(" FROM Game g2")
        sb.append(" WHERE g2.DartsMatchId = g.DartsMatchId")
        sb.append(" AND g2.MatchOrdinal = g.MatchOrdinal")
        sb.append(" AND g2.RowId > g.RowId")
        sb.append(")")

        val whereSql = sb.toString()
        val games = GameEntity().retrieveEntities(whereSql, "g")
        val count = games.size
        if (count > 0)
        {
            return listOf(SanityCheckResultDuplicateMatchOrdinals(games))
        }

        return listOf()
    }
}