package dartzee.stats

import dartzee.core.obj.HashMapCount
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.database

fun getGameCounts(player: PlayerEntity): HashMapCount<GameType>
{
    val hmTypeToCount = HashMapCount<GameType>()
    val query = "SELECT g.GameType FROM Participant pt, Game g WHERE pt.GameId = g.RowId AND pt.PlayerId = '${player.rowId}'"
    database.executeQuery(query).use { rs ->
        while (rs.next())
        {
            val gameType = GameType.valueOf(rs.getString("GameType"))
            hmTypeToCount.incrementCount(gameType)
        }
    }

    return hmTypeToCount
}