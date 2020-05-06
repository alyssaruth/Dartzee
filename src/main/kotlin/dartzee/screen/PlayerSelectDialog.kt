package dartzee.screen

import dartzee.bean.PlayerTypeFilterPanel
import dartzee.bean.getSelectedPlayers
import dartzee.bean.initPlayerTableModel
import dartzee.core.bean.IDoubleClickListener
import dartzee.core.bean.ScrollTable
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.db.PlayerEntity
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.ListSelectionModel

class PlayerSelectDialog(selectionMode: Int) : SimpleDialog(), IDoubleClickListener
{
    var selectedPlayers = listOf<PlayerEntity>()
    var playersToExclude = listOf<PlayerEntity>()

    val panelNorth = PlayerTypeFilterPanel()
    val tablePlayers = ScrollTable()

    init
    {
        title = "Select Player(s)"
        setSize(300, 300)
        isModal = true

        contentPane.add(panelNorth, BorderLayout.NORTH)
        contentPane.add(tablePlayers, BorderLayout.CENTER)
        tablePlayers.setSelectionMode(selectionMode)
        tablePlayers.addDoubleClickListener(this)

        panelNorth.addActionListener(this)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (panelNorth.isEventSource(arg0))
        {
            buildTable()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    override fun doubleClicked(source: Component)
    {
        okPressed()
    }

    fun buildTable()
    {
        val whereSql = panelNorth.getWhereSql()
        val allPlayers = PlayerEntity.retrievePlayers(whereSql)

        val players = allPlayers.filter{ p -> playersToExclude.none{ it.rowId == p.rowId} }
        tablePlayers.initPlayerTableModel(players)
    }

    override fun okPressed()
    {
        selectedPlayers = tablePlayers.getSelectedPlayers()
        if (selectedPlayers.isEmpty())
        {
            DialogUtil.showError("You must select at least one player.")
            return
        }

        dispose()
    }

    companion object
    {
        fun selectPlayer(): PlayerEntity?
        {
            val players = selectPlayers(listOf(), ListSelectionModel.SINGLE_SELECTION)
            return if (players.isEmpty()) null else players.first()
        }

        fun selectPlayers(playersToExclude: List<PlayerEntity>): List<PlayerEntity>
        {
            return selectPlayers(playersToExclude, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        }

        private fun selectPlayers(playersToExclude: List<PlayerEntity>, selectionMode: Int): List<PlayerEntity>
        {
            val dialog = PlayerSelectDialog(selectionMode)
            dialog.playersToExclude = playersToExclude
            dialog.buildTable()
            dialog.setLocationRelativeTo(null)
            dialog.isVisible = true

            return dialog.selectedPlayers
        }
    }
}
