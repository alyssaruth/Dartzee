package burlton.dartzee.code.screen.stats.player

import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.stats.GameWrapper
import burlton.desktopcore.code.util.TableUtil
import burlton.desktopcore.code.util.TableUtil.SimpleRenderer
import java.awt.Color
import java.awt.GridLayout
import javax.swing.SwingConstants

class StatisticsTabX01TopFinishes : AbstractStatisticsTab()
{
    private val tableTopFinishesMine = ScrollTableDartsGame()
    private val tableTopFinishesOther = ScrollTableDartsGame()

    init
    {
        layout = GridLayout(0, 2, 0, 0)

        add(tableTopFinishesMine)
        add(tableTopFinishesOther)

        tableTopFinishesOther.tableForeground = Color.RED
    }

    override fun populateStats()
    {
        setOtherComponentVisibility(this, tableTopFinishesOther)

        buildTopFinishesTable(filteredGames, tableTopFinishesMine)
        if (includeOtherComparison())
        {
            buildTopFinishesTable(filteredGamesOther, tableTopFinishesOther)
        }
    }

    private fun buildTopFinishesTable(games: List<GameWrapper>, table: ScrollTableDartsGame)
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Finish")
        model.addColumn("Darts")
        model.addColumn("Game")

        //Sort by checkout total.
        val sortedGames = games.sortedByDescending { it.getCheckoutTotal() }

        val listSize = Math.min(MAX_FINISHES_TO_SHOW, games.size)
        for (i in 0 until listSize)
        {
            val game = sortedGames[i]
            if (!game.isFinished())
            {
                continue
            }

            val gameId = game.localId
            val total = game.getCheckoutTotal()

            val dartStr = game.getDartsForFinalRound().joinToString()

            val row = arrayOf(total, dartStr, gameId)
            model.addRow(row)
        }

        table.model = model
        table.setRenderer(0, SimpleRenderer(SwingConstants.LEFT, null))
        table.sortBy(0, true)
    }

    companion object
    {
        private const val MAX_FINISHES_TO_SHOW = 25
    }
}
