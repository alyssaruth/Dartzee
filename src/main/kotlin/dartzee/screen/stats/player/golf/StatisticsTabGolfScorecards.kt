package dartzee.screen.stats.player.golf

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.RowSelectionListener
import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.screen.stats.player.AbstractStatisticsTab
import dartzee.stats.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class StatisticsTabGolfScorecards : AbstractStatisticsTab(), ActionListener, RowSelectionListener
{
    private var mode: GolfMode = GolfMode.FULL_18
    private val comboBoxMode = JComboBox<ComboBoxItem<GolfMode>>()
    private val panelCenter = JPanel()
    private val lblMode = JLabel("Mode")
    private val panelMine = JPanel()
    private val panelOther = JPanel()
    private val scrollTableMine = ScrollTableDartsGame()
    private val panelMyScorecard = JPanel()
    private val scrollTableOther = ScrollTableDartsGame()
    private val panelOtherScorecard = JPanel()

    init
    {
        layout = BorderLayout(0, 0)

        val panelMode = JPanel()
        add(panelMode, BorderLayout.NORTH)
        panelMode.layout = FlowLayout(FlowLayout.LEADING, 5, 5)
        panelMode.add(lblMode)
        panelMode.add(comboBoxMode)
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = GridLayout(0, 2, 0, 0)
        panelCenter.add(panelMine)
        panelMine.layout = GridLayout(0, 2, 0, 0)
        panelMine.add(scrollTableMine)
        panelMine.add(panelMyScorecard)
        panelMyScorecard.layout = BorderLayout(0, 0)
        panelCenter.add(panelOther)
        panelOther.layout = GridLayout(0, 2, 0, 0)
        panelOther.add(scrollTableOther)
        scrollTableOther.tableForeground = Color.RED
        panelOther.add(panelOtherScorecard)
        panelOtherScorecard.layout = BorderLayout(0, 0)
        scrollTableMine.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        scrollTableOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        comboBoxMode.addActionListener(this)
        scrollTableMine.addRowSelectionListener(this)
        scrollTableOther.addRowSelectionListener(this)
    }

    override fun populateStats() = populateStats(true)

    private fun populateStats(rebuildComboBox: Boolean)
    {
        if (rebuildComboBox)
        {
            initialiseComboBoxModel()
        }

        //Hide or show the 'other' panel depending on whether there's a comparison
        setOtherComponentVisibility(panelCenter, panelOther)

        //Update the mode based on what's selected in the combo box
        val ix = comboBoxMode.selectedIndex
        val item = comboBoxMode.getItemAt(ix)
        mode = item.hiddenData

        //And now populate the table(s)
        populateTable(filteredGames, scrollTableMine)
        if (includeOtherComparison())
        {
            populateTable(filteredGamesOther, scrollTableOther)
        }
    }

    private fun initialiseComboBoxModel()
    {
        val model = DefaultComboBoxModel<ComboBoxItem<GolfMode>>()
        addMode("Front 9", GolfMode.FRONT_9, model)
        addMode("Back 9", GolfMode.BACK_9, model)
        addMode("Full 18", GolfMode.FULL_18, model)

        if (model.size == 0)
        {
            model.addElement(ComboBoxItem(GolfMode.FULL_18, "N/A"))
        }

        comboBoxMode.model = model
    }

    private fun addMode(modeDesc: String, mode: GolfMode, model: DefaultComboBoxModel<ComboBoxItem<GolfMode>>)
    {
        val validGames = filteredGames.filter { it.getRoundScore(mode) > -1 }
        if (validGames.isEmpty())
        {
            return
        }

        val item = ComboBoxItem(mode, modeDesc)
        model.addElement(item)
    }


    private fun populateTable(filteredGames: List<GameWrapper>, scrollTable: ScrollTableDartsGame)
    {
        //Filter out the -1's - these are games that haven't gone on long enough to have all the data
        val validGames = filteredGames.filter { it.getRoundScore(mode) > -1 }

        //Populate the table from the wrappers
        val model = TableUtil.DefaultModel()
        model.addColumn("Game")
        model.addColumn("Score")
        model.addColumn("!GameObject")

        val rows = validGames.map{ arrayOf(it.localId, it.getRoundScore(mode), it) }
        model.addRows(rows)

        scrollTable.model = model
        scrollTable.removeColumn(2)
        scrollTable.sortBy(1, false)

        //Select a row so the scorecard automatically populates
        if (!validGames.isEmpty())
        {
            scrollTable.selectFirstRow()
        }
    }

    private fun displayScorecard(game: GameWrapper, scorecardPanel: JPanel)
    {
        val fudgeFactor = if (mode == GolfMode.BACK_9) 9 else 0
        val scorer = GolfStatsScoresheet(fudgeFactor, showGameId = false)

        val rounds = game.getGolfRounds(mode)
        scorer.populateTable(rounds)

        scorecardPanel.removeAll()
        scorecardPanel.add(scorer, BorderLayout.CENTER)
        scorecardPanel.revalidate()
        scorecardPanel.repaint()
    }

    override fun actionPerformed(e: ActionEvent) = populateStats(false)

    override fun selectionChanged(src: ScrollTable)
    {
        val row = src.selectedModelRow
        if (row == -1)
        {
            return
        }

        val game = src.getValueAt(row, 2) as GameWrapper
        when (src)
        {
            scrollTableMine -> displayScorecard(game, panelMyScorecard)
            scrollTableOther -> displayScorecard(game, panelOtherScorecard)
        }
    }
}
