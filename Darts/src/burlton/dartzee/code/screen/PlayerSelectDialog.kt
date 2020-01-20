package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.getSelectedPlayers
import burlton.dartzee.code.bean.initPlayerTableModel
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.core.bean.ScrollTable
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.ListSelectionModel

class PlayerSelectDialog(selectionMode: Int) : SimpleDialog()
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

    fun buildTable()
    {
        val whereSql = panelNorth.getWhereSql()
        val allPlayers = PlayerEntity.retrievePlayers(whereSql, false)

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
