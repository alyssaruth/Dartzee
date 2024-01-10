package dartzee.screen.game.rtc

import dartzee.core.util.minOrZero

class MatchStatisticsPanelRoundTheClock(gameParams: String) :
    GameStatisticsPanelRoundTheClock(gameParams) {
    override fun addRowsToTable() {
        super.addRowsToTable()

        addRow(getBestGameRow { it.minOrZero() })
        addRow(getAverageGameRow())
    }

    override fun getRankedRowsLowestWins() =
        super.getRankedRowsLowestWins() + "Best Game" + "Avg Game"
}
