package burlton.dartzee.code.bean

import burlton.dartzee.code.`object`.GameLauncher
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.core.bean.ScrollTableHyperlink
import burlton.dartzee.code.core.util.DialogUtil

class ScrollTableDartsGame(linkColumnName: String = "Game") : ScrollTableHyperlink(linkColumnName)
{
    override fun linkClicked(value: Any)
    {
        val localId = value as Long
        if (localId > 0)
        {
            val gameId = GameEntity.getGameId(localId)
            gameId?.let{ GameLauncher.loadAndDisplayGame(gameId) }
        }
        else
        {
            DialogUtil.showError("It isn't possible to display individual games from a simulation.")
        }
    }

    override fun renderValue(value: Any): String
    {
        return "#$value"
    }
}
