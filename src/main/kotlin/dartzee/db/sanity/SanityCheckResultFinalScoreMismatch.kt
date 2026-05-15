package dartzee.db.sanity

import dartzee.bean.ScrollTableDartsGame
import dartzee.game.GameType
import javax.swing.table.DefaultTableModel

class SanityCheckResultFinalScoreMismatch(gameType: GameType, tableModel: DefaultTableModel) :
    SanityCheckResult(
        tableModel,
        "FinalScores that don't match the raw data (${gameType.getDescription()})",
    ) {

    override fun getScrollTable() = ScrollTableDartsGame("GameId")
}
