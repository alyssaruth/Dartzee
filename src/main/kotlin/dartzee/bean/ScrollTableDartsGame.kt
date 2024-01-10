package dartzee.bean

import dartzee.core.bean.ScrollTableHyperlink
import dartzee.core.util.DialogUtil
import dartzee.db.GameEntity
import dartzee.utils.InjectedThings.gameLauncher

class ScrollTableDartsGame(linkColumnName: String = "Game", testId: String = "") :
    ScrollTableHyperlink(linkColumnName, testId) {
    override fun linkClicked(value: Any) {
        val localId = value as Long
        if (localId > 0) {
            val gameId = GameEntity.getGameId(localId)
            gameId?.let { gameLauncher.loadAndDisplayGame(gameId) }
        } else {
            DialogUtil.showErrorOLD(
                "It isn't possible to display individual games from a simulation."
            )
        }
    }

    override fun renderValue(value: Any) = "#$value"
}
