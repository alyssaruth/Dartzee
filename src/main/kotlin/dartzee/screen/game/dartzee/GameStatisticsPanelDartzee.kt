package dartzee.screen.game.dartzee

import dartzee.game.state.DartzeePlayerState
import dartzee.screen.game.AbstractGameStatisticsPanel

open class GameStatisticsPanelDartzee(gameParams: String): AbstractGameStatisticsPanel<DartzeePlayerState>(gameParams)
{
    override fun getRankedRowsHighestWins(): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRankedRowsLowestWins(): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHistogramRows(): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStartOfSectionRows(): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addRowsToTable() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}