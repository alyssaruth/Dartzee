package dartzee.screen.stats.player.x01

import dartzee.bean.ScrollTableDartsGame
import dartzee.bean.X01ScoreRenderer
import dartzee.core.bean.NumberField
import dartzee.core.bean.RowSelectionListener
import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.core.util.TableUtil.SimpleRenderer
import dartzee.screen.stats.player.AbstractStatisticsTab
import dartzee.stats.GameWrapper
import dartzee.stats.ThreeDartScoreWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants

class StatisticsTabX01ThreeDartScores : AbstractStatisticsTab(), RowSelectionListener
{
    private val panelTables = JPanel()
    private val tableBreakdownMine = ScrollTableDartsGame("Example Game")
    private val tableBreakdownOther = ScrollTableDartsGame("Example Game")
    private val panelConfig = JPanel()
    private val lblScoreThreshold = JLabel("Score Threshold")
    private val nfScoreThreshold = NumberField(62, 300)
    private val tableScoresMine = ScrollTable()
    private val tableScoresOther = ScrollTable()

    init
    {
        nfScoreThreshold.columns = 10
        layout = BorderLayout(0, 0)

        add(panelTables, BorderLayout.CENTER)
        panelTables.layout = GridLayout(2, 2, 0, 0)
        panelTables.add(tableScoresMine)
        tableScoresOther.tableForeground = Color.RED
        panelTables.add(tableScoresOther)
        panelTables.add(tableBreakdownMine)
        panelTables.add(tableBreakdownOther)
        tableBreakdownOther.tableForeground = Color.RED

        add(panelConfig, BorderLayout.NORTH)
        panelConfig.add(lblScoreThreshold)
        panelConfig.add(nfScoreThreshold)
        nfScoreThreshold.value = 140
        tableScoresMine.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tableScoresOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        nfScoreThreshold.addPropertyChangeListener(this)
        tableScoresMine.addRowSelectionListener(this)
        tableScoresOther.addRowSelectionListener(this)
    }

    override fun populateStats()
    {
        setComponentVisibility()

        populateTable(tableScoresMine, filteredGames)
        if (includeOtherComparison())
        {
            populateTable(tableScoresOther, filteredGamesOther)
        }
    }

    private fun setComponentVisibility()
    {
        panelTables.removeAll()

        if (includeOtherComparison())
        {
            panelTables.layout = GridLayout(2, 2, 0, 0)
            panelTables.add(tableScoresMine)
            panelTables.add(tableScoresOther)
            panelTables.add(tableBreakdownMine)
            panelTables.add(tableBreakdownOther)
        }
        else
        {
            panelTables.layout = GridLayout(2, 1, 0, 0)
            panelTables.add(tableScoresMine)
            panelTables.add(tableBreakdownMine)
        }
    }

    private fun populateTable(table: ScrollTable, filteredGames: List<GameWrapper>)
    {
        //Sort by start date
        val sortedGames = filteredGames.sortedBy { it.dtStart }

        //Build up two maps, one of score to count (e.g. 20, 5, 1 -> 10) and the other of score to example game
        val hmScoreToThreeDartBreakdown = mutableMapOf<Int, ThreeDartScoreWrapper>()
        for (game in sortedGames)
        {
            game.populateThreeDartScoreMap(hmScoreToThreeDartBreakdown, nfScoreThreshold.getNumber())
        }

        val model = DefaultModel()
        model.addColumn("Score")
        model.addColumn("Count")
        model.addColumn("!Wrapper")

        val rows = hmScoreToThreeDartBreakdown.entries.map { (score, wrapper)
            -> arrayOf(score, wrapper.getTotalCount(), wrapper)
        }

        model.addRows(rows)

        table.model = model

        table.setRenderer(0, X01ScoreRenderer())
        table.setRenderer(1, SimpleRenderer(SwingConstants.LEFT, null))

        table.selectFirstRow()

        table.removeColumn(2)
        table.sortBy(0, false)
    }


    private fun populateBreakdownTable(table: ScrollTableDartsGame, wrapper: ThreeDartScoreWrapper)
    {
        val model = DefaultModel()
        model.addColumn("Method")
        model.addColumn("Count")
        model.addColumn("Example Game")

        model.addRows(wrapper.createRows())

        table.model = model

        table.setRenderer(1, SimpleRenderer(SwingConstants.LEFT, null))

        val footerRow = arrayOf("Total", wrapper.getTotalCount(), "-")
        table.addFooterRow(footerRow)

        table.sortBy(1, true)
    }

    override fun selectionChanged(src: ScrollTable)
    {
        val selectedRow = src.selectedModelRow
        if (selectedRow == -1)
        {
            return
        }

        val wrapper = src.getValueAt(selectedRow, 2) as ThreeDartScoreWrapper

        when (src)
        {
            tableScoresMine -> populateBreakdownTable(tableBreakdownMine, wrapper)
            tableScoresOther -> populateBreakdownTable(tableBreakdownOther, wrapper)
        }
    }
}