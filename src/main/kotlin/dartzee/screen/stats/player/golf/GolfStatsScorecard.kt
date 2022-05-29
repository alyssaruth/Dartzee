package dartzee.screen.stats.player.golf

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.util.TableUtil
import dartzee.screen.game.scorer.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JPanel

class GolfStatsScorecard(override val fudgeFactor: Int, private val showGameId: Boolean, val testId: String = "") : JPanel(), IGolfScorerTable
{
    override val model = TableUtil.DefaultModel()
    override fun getNumberOfColumns() = if (showGameId) 6 else 5

    val tableScores = ScrollTableDartsGame()

    init
    {
        layout = BorderLayout(0, 0)
        add(tableScores, BorderLayout.CENTER)
        tableScores.setFillsViewportHeight(false)
        tableScores.setShowRowCount(false)
        tableScores.disableSorting()
        preferredSize = Dimension(SCORER_WIDTH, 600)

        initialiseTable()
    }

    private fun initialiseTable()
    {
        tableScores.setRowHeight(25)
        for (i in 0 until getNumberOfColumns())
        {
            model.addColumn("")
        }
        tableScores.model = model

        for (i in 0..GOLF_SCORE_COLUMN)
        {
            tableScores.setRenderer(i, GolfDartRenderer(showGameId))
        }

        if (showGameId)
        {
            tableScores.setLinkColumnIndex(tableScores.columnCount - 1)
        }
    }

    fun addGameIds(localGameIds: List<Long>)
    {
        localGameIds.forEachIndexed { ix, gameId ->
            val row = if (ix >= 9) ix + 1 else ix
            model.setValueAt(gameId, row, GOLF_GAME_ID_COLUMN)
        }
    }

    fun setTableForeground(color: Color)
    {
        tableScores.tableForeground = color
    }
}