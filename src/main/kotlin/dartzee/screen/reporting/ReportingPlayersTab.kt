package dartzee.screen.reporting

import dartzee.bean.getSelectedPlayer
import dartzee.bean.getSelectedPlayers
import dartzee.bean.initPlayerTableModel
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.bean.RowSelectionListener
import dartzee.core.bean.ScrollTable
import dartzee.core.util.DialogUtil
import dartzee.db.PlayerEntity
import dartzee.reporting.ReportParameters
import dartzee.screen.PlayerSelectDialog
import dartzee.screen.ScreenCache
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class ReportingPlayersTab: JPanel(), ActionListener, RowSelectionListener
{
    private val hmPlayerToParametersPanel = mutableMapOf<PlayerEntity, PlayerParametersPanel>()

    private val panelNorth = JPanel()
    val checkBoxExcludeOnlyAi = JCheckBox("Exclude games with only AI players")
    private val lblIncludeMode = JLabel("Include games with:")
    private val panelRdbtns = RadioButtonPanel()
    val rdbtnInclude = JRadioButton("All the selected players")
    val rdbtnExclude = JRadioButton("None of the selected players")
    private val panelTable = JPanel()
    private val panelTableButtons = JPanel()
    val scrollTable = ScrollTable()
    private val btnAddPlayer = JButton("")
    val btnRemovePlayer = JButton("")

    var includedPlayerPanel = PlayerParametersPanel().also { it.disableAll() }

    init
    {
        layout = BorderLayout(0, 0)

        add(panelNorth, BorderLayout.NORTH)
        panelNorth.layout = MigLayout("", "[]", "[][]")
        panelNorth.add(checkBoxExcludeOnlyAi, "cell 0 0")
        panelNorth.add(panelRdbtns, "cell 0 1")
        panelRdbtns.add(lblIncludeMode)
        panelRdbtns.add(rdbtnInclude)
        panelRdbtns.add(rdbtnExclude)
        panelTable.layout = BorderLayout(0, 0)
        panelTable.add(panelTableButtons, BorderLayout.NORTH)
        panelTable.add(scrollTable, BorderLayout.CENTER)
        val flowLayout = panelTableButtons.layout as FlowLayout
        flowLayout.alignment = FlowLayout.LEFT
        btnAddPlayer.preferredSize = Dimension(30, 30)
        btnAddPlayer.icon = ImageIcon(ReportingSetupScreen::class.java.getResource("/buttons/addPlayer.png"))
        panelTableButtons.add(btnAddPlayer)
        btnRemovePlayer.icon = ImageIcon(ReportingSetupScreen::class.java.getResource("/buttons/removePlayer.png"))
        btnRemovePlayer.preferredSize = Dimension(30, 30)
        panelTableButtons.add(btnRemovePlayer)
        add(panelTable, BorderLayout.CENTER)
        add(includedPlayerPanel, BorderLayout.SOUTH)

        panelRdbtns.addActionListener(this)
        btnAddPlayer.addActionListener(this)
        btnRemovePlayer.addActionListener(this)
        scrollTable.addRowSelectionListener(this)

        scrollTable.setRowName("player")
        scrollTable.initPlayerTableModel(emptyList())
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        when (e?.source)
        {
            btnAddPlayer -> addPlayers()
            btnRemovePlayer -> removePlayers()
            rdbtnInclude, rdbtnExclude -> togglePlayerParameters()
        }
    }

    private fun togglePlayerParameters()
    {
        if (rdbtnInclude.isSelected)
        {
            add(includedPlayerPanel, BorderLayout.SOUTH)
        }
        else
        {
            remove(includedPlayerPanel)
        }

        ScreenCache.mainScreen.pack()
        repaint()
    }

    private fun addPlayers()
    {
        val allSelected = hmPlayerToParametersPanel.keys.toList()
        val players = PlayerSelectDialog.selectPlayers(allSelected)
        addPlayers(players)
    }

    fun addPlayers(players: List<PlayerEntity>)
    {
        for (player in players)
        {
            hmPlayerToParametersPanel[player] = PlayerParametersPanel()
        }

        scrollTable.initPlayerTableModel(hmPlayerToParametersPanel.keys.toList())
        scrollTable.selectFirstRow()
    }

    private fun removePlayers()
    {
        val playersToRemove = scrollTable.getSelectedPlayers()
        if (playersToRemove.isEmpty())
        {
            DialogUtil.showErrorOLD("You must select player(s) to remove.")
            return
        }

        playersToRemove.forEach { hmPlayerToParametersPanel.remove(it) }
        scrollTable.initPlayerTableModel(hmPlayerToParametersPanel.keys.toList())
    }

    override fun selectionChanged(src: ScrollTable)
    {
        remove(includedPlayerPanel)

        val player = scrollTable.getSelectedPlayer()
        if (player == null)
        {
            includedPlayerPanel = PlayerParametersPanel().also { it.disableAll() }
        }
        else
        {
            includedPlayerPanel = hmPlayerToParametersPanel[player]!!
        }

        if (rdbtnInclude.isSelected)
        {
            add(includedPlayerPanel, BorderLayout.SOUTH)
        }

        ScreenCache.mainScreen.pack()
        repaint()
    }

    fun valid(): Boolean
    {
        if (rdbtnInclude.isSelected)
        {
            val entries = hmPlayerToParametersPanel.entries
            return entries.all { it.value.valid(it.key) }
        }

        return true
    }

    fun populateReportParameters(rp: ReportParameters)
    {
        rp.excludeOnlyAi = checkBoxExcludeOnlyAi.isSelected

        if (rdbtnInclude.isSelected)
        {
            rp.hmIncludedPlayerToParms = hmPlayerToParametersPanel.mapValues { entry -> entry.value.generateParameters() }
        }
        else
        {
            rp.excludedPlayers = hmPlayerToParametersPanel.keys.toList()
        }
    }
}