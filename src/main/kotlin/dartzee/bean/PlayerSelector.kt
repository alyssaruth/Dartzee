package dartzee.bean

import dartzee.core.bean.IDoubleClickListener
import dartzee.core.bean.ScrollTable
import dartzee.core.bean.ScrollTableOrdered
import dartzee.core.util.DialogUtil
import dartzee.db.MAX_PLAYERS
import dartzee.db.PlayerEntity
import dartzee.utils.DartsColour
import dartzee.utils.FeatureToggles
import dartzee.utils.translucent
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.table.TableCellRenderer

class PlayerSelector : JPanel(), ActionListener, IDoubleClickListener
{
    val tablePlayersToSelectFrom = ScrollTable()
    val tablePlayersSelected = ScrollTableOrdered()
    val btnSelect = JButton("")
    val btnUnselect = JButton("")
    private val btnPairs = JToggleButton("")

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
        btnPairs.icon = ImageIcon(javaClass.getResource("/buttons/teams.png"))
        btnPairs.toolTipText = "Play in pairs"
        tablePlayersSelected.addButtonToOrderingPanel(btnPairs, 3)

        tablePlayersSelected.addDoubleClickListener(this)
        tablePlayersToSelectFrom.addDoubleClickListener(this)

        btnSelect.addActionListener(this)
        btnUnselect.addActionListener(this)
        btnPairs.addActionListener(this)

        addKeyListener(tablePlayersSelected)
        addKeyListener(tablePlayersToSelectFrom)
    }

    fun init()
    {
        val allPlayers = PlayerEntity.retrievePlayers("")
        tablePlayersToSelectFrom.initPlayerTableModel(allPlayers)
        tablePlayersSelected.initPlayerTableModel()

        val nimbusRenderer = tablePlayersSelected.getBuiltInRenderer()
        tablePlayersSelected.setTableRenderer(TeamRenderer(nimbusRenderer))

        btnPairs.isVisible = FeatureToggles.teamMode
    }

    fun init(selectedPlayers: List<PlayerEntity>)
    {
        init()
        moveRows(tablePlayersToSelectFrom, tablePlayersSelected, selectedPlayers)
    }

    fun getSelectedPlayers(): List<PlayerEntity> = tablePlayersSelected.getAllPlayers()

    private fun addKeyListener(table: ScrollTable)
    {
        table.addKeyAction(KeyEvent.VK_ENTER) { moveRows(table) }
    }

    private fun moveRows(source: ScrollTable, destination: ScrollTable)
    {
        moveRows(source, destination, source.getSelectedPlayers())
    }
    private fun moveRows(source: ScrollTable, destination: ScrollTable, selectedPlayers: List<PlayerEntity>)
    {
        destination.addPlayers(selectedPlayers)

        var rowToSelect = source.selectedViewRow

        val allPlayers = source.getAllPlayers()
        val availablePlayers = allPlayers.filter{ p -> selectedPlayers.none{ it.rowId == p.rowId} }
        source.initPlayerTableModel(availablePlayers)

        if (rowToSelect > availablePlayers.size - 1)
        {
            rowToSelect = 0
        }

        if (availablePlayers.isNotEmpty())
        {
            source.selectRow(rowToSelect)
        }
    }

    /**
     * Is this selection valid for a game/match?
     */
    fun valid(match: Boolean): Boolean
    {
        val selectedPlayers = getSelectedPlayers()
        val rowCount = selectedPlayers.size
        if (rowCount < 1)
        {
            DialogUtil.showError("You must select at least 1 player.")
            return false
        }

        val playerOrTeamDesc = if (btnPairs.isSelected) "teams" else "players"
        val matchMinimum = if (btnPairs.isSelected) 4 else 2
        if (match && rowCount < matchMinimum)
        {
            DialogUtil.showError("You must select at least 2 $playerOrTeamDesc for a match.")
            return false
        }

        val maxPlayers = if (btnPairs.isSelected) MAX_PLAYERS * 2 else MAX_PLAYERS
        if (rowCount > maxPlayers)
        {
            DialogUtil.showError("You cannot select more than $MAX_PLAYERS $playerOrTeamDesc.")
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

    private fun togglePairs()
    {
        tablePlayersSelected.repaint()
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source)
        {
            btnSelect, btnUnselect -> moveRows(e.source)
            btnPairs -> togglePairs()
        }
    }
    override fun doubleClicked(source: Component) = moveRows(source)

    private inner class TeamRenderer(private val baseRenderer: TableCellRenderer): TableCellRenderer
    {
        private val colors = listOf(Color.RED, Color.GREEN, Color.CYAN, Color.YELLOW, DartsColour.PURPLE, DartsColour.ORANGE)

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            val c = baseRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JComponent

            val rawColour = if (row/2 < colors.size) colors[row/2] else null
            if (btnPairs.isSelected)
            {
                c.background = if (isSelected) rawColour else rawColour?.translucent()
                c.foreground = Color.BLACK

                val padding = if (column == 0) 0 else 5
                val lineBorder = MatteBorder(0, 0, row % 2, 0, Color.BLACK)
                val padBorder = EmptyBorder(0, padding, 0, 0)
                c.border = CompoundBorder(lineBorder, padBorder)
            }
            else if (!isSelected)
            {
                c.background = null
            }

            return c
        }
    }
}
