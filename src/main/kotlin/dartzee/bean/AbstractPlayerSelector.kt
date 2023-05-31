package dartzee.bean

import dartzee.core.bean.IDoubleClickListener
import dartzee.core.bean.ScrollTable
import dartzee.db.PlayerEntity
import dartzee.logging.CODE_SWING_ERROR
import dartzee.utils.InjectedThings
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class PlayerSelector: AbstractPlayerSelector<ScrollTable>()
{
    override val tablePlayersSelected = ScrollTable()

    init
    {
        super.render()
    }
}

abstract class AbstractPlayerSelector<S: ScrollTable> : JPanel(), ActionListener, IDoubleClickListener
{
    abstract val tablePlayersSelected: S

    val tablePlayersToSelectFrom = ScrollTable()
    private val btnSelect = JButton("")
    private val btnUnselect = JButton("")

    protected fun render()
    {
        layout = MigLayout("", "[452px][100px][452px]", "[407px]")

        val panelMovementOptions = JPanel()
        add(tablePlayersToSelectFrom, "cell 0 0,alignx left,growy")
        panelMovementOptions.minimumSize = Dimension(50, 10)
        add(panelMovementOptions, "cell 1 0,grow")
        panelMovementOptions.layout = MigLayout("al center center, wrap, gapy 20")
        btnSelect.name = "Select"
        btnSelect.icon = ImageIcon(AbstractPlayerSelector::class.java.getResource("/buttons/rightArrow.png"))
        btnSelect.preferredSize = Dimension(40, 40)
        panelMovementOptions.add(btnSelect, "cell 0 0,alignx left,aligny top")
        btnUnselect.name = "Unselect"
        btnUnselect.icon = ImageIcon(AbstractPlayerSelector::class.java.getResource("/buttons/leftArrow.png"))
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

    open fun init()
    {
        val allPlayers = PlayerEntity.retrievePlayers("")
        tablePlayersToSelectFrom.initPlayerTableModel(allPlayers)
        tablePlayersSelected.initPlayerTableModel()
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

    private fun moveRows(source: Any)
    {
        when (source)
        {
            tablePlayersToSelectFrom, btnSelect -> moveRows(tablePlayersToSelectFrom, tablePlayersSelected)
            tablePlayersSelected, btnUnselect -> moveRows(tablePlayersSelected, tablePlayersToSelectFrom)
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source)
        {
            btnSelect, btnUnselect -> moveRows(e.source)
            else -> InjectedThings.logger.error(CODE_SWING_ERROR, "Unexpected actionPerformed: ${e.source}")
        }
    }
    override fun doubleClicked(source: Component) = moveRows(source)
}
