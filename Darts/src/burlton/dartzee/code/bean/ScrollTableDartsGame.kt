package burlton.dartzee.code.bean

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.desktopcore.code.bean.ScrollTableHyperlink
import burlton.desktopcore.code.util.DialogUtil

class ScrollTableDartsGame(linkColumnName: String = "Game") : ScrollTableHyperlink(linkColumnName)
{
    override fun linkClicked(value: Any)
    {
        val localId = value as Long
        if (localId > 0)
        {
            val gameId = GameEntity.getGameId(localId)
            DartsGameScreen.loadAndDisplayGame(gameId)
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
