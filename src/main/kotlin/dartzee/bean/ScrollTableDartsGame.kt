package dartzee.bean

import dartzee.`object`.GameLauncher
import dartzee.core.bean.ScrollTableHyperlink
import dartzee.core.util.DialogUtil
import dartzee.db.GameEntity

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
