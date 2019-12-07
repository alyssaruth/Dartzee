package burlton.dartzee.code.bean

import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.bean.DoubleClickListener
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.bean.ScrollTableOrdered
import burlton.desktopcore.code.util.DialogUtil
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class PlayerSelector : JPanel(), ActionListener, DoubleClickListener
{
    val tablePlayersToSelectFrom = ScrollTable()
    val tablePlayersSelected = ScrollTableOrdered()
    val btnSelect = JButton("")
    val btnUnselect = JButton("")

    init
    {
        layout = MigLayout("", "[452px][100px][452px]", "[407px]")

        val panelMovementOptions = JPanel()
        add(tablePlayersToSelectFrom, "cell 0 0,alignx left,growy")
        panelMovementOptions.minimumSize = Dimension(50, 10)
        add(panelMovementOptions, "cell 1 0,grow")
        panelMovementOptions.layout = MigLayout("al center center, wrap, gapy 20")
        btnSelect.icon = ImageIcon(PlayerSelector::class.java.getResource("/buttons/rightArrow.png"))
        btnSelect.preferredSize = Dimension(40, 40)
        panelMovementOptions.add(btnSelect, "cell 0 0,alignx left,aligny top")
        btnUnselect.icon = ImageIcon(PlayerSelector::class.java.getResource("/buttons/leftArrow.png"))
        btnUnselect.preferredSize = Dimension(40, 40)
        panelMovementOptions.add(btnUnselect, "cell 0 1,alignx left,aligny top")
        add(tablePlayersSelected, "cell 2 0,alignx left,growy")

        tablePlayersSelected.addDoubleClickListener(this)
        tablePlayersToSelectFrom.addDoubleClickListener(this)

        btnSelect.addActionListener(this)
        btnUnselect.addActionListener(this)

        addKeyListener(tablePlayersSelected)
        addKeyListener(tablePlayersToSelectFrom)
    }

    fun init()
    {
        val allPlayers = PlayerEntity.retrievePlayers("", false)
        tablePlayersToSelectFrom.initTableModel(allPlayers)
        tablePlayersSelected.initTableModel()
    }

    fun init(selectedPlayers: List<PlayerEntity>)
    {
        init()
        moveRows(tablePlayersToSelectFrom, tablePlayersSelected, selectedPlayers)
    }

    fun getSelectedPlayers(): MutableList<PlayerEntity> = tablePlayersSelected.getAllPlayers()

    private fun addKeyListener(table: ScrollTable)
    {
        table.addKeyAction(KeyEvent.VK_ENTER, "Enter", object : AbstractAction()
        {
            override fun actionPerformed(ae: ActionEvent)
            {
                moveRows(table)
            }
        })
    }

    private fun moveRows(source: ScrollTable, destination: ScrollTable)
    {
        val selectedPlayers = source.getSelectedPlayers()
        if (selectedPlayers.isEmpty())
        {
            //Nothing to do
            return
        }

        moveRows(source, destination, selectedPlayers)
    }
    private fun moveRows(source: ScrollTable, destination: ScrollTable, selectedPlayers: List<PlayerEntity>)
    {
        destination.addPlayers(selectedPlayers)

        var rowToSelect = source.selectedViewRow

        val allPlayers = source.getAllPlayers()
        val availablePlayers = allPlayers.filter{ p -> selectedPlayers.none{ it.rowId == p.rowId} }
        source.initTableModel(availablePlayers)

        if (rowToSelect > availablePlayers.size - 1)
        {
            rowToSelect = 0
        }

        if (!availablePlayers.isEmpty())
        {
            source.selectRow(rowToSelect)
        }
    }

    /**
     * Is this selection valid for a game/match?
     */
    fun valid(match: Boolean, gameType: Int): Boolean
    {
        val selectedPlayers = getSelectedPlayers()
        val rowCount = selectedPlayers.size
        if (rowCount < 1)
        {
            DialogUtil.showError("You must select at least 1 player.")
            return false
        }

        if (match && rowCount < 2)
        {
            DialogUtil.showError("You must select at least 2 players for a match.")
            return false
        }

        if (rowCount > 6)
        {
            DialogUtil.showError("You cannot select more than 6 players.")
            return false
        }

        //Temporary measure until https://trello.com/c/T89Kqmxj is implemented
        if (gameType == GAME_TYPE_DARTZEE && selectedPlayers.any { it.isAi() })
        {
            DialogUtil.showError("You cannot select AI opponents for Dartzee.")
            return false
        }

        return true
    }

    private fun moveRows(source: Any)
    {
        when (source)
        {
            tablePlayersToSelectFrom, btnSelect -> moveRows(tablePlayersToSelectFrom, tablePlayersSelected)
            tablePlayersSelected, btnUnselect -> moveRows(tablePlayersSelected, tablePlayersToSelectFrom)
        }
    }

    override fun actionPerformed(e: ActionEvent) = moveRows(e.source)
    override fun doubleClicked(source: Component) = moveRows(source)
}
