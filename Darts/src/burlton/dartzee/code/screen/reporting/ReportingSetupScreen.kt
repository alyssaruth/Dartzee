package burlton.dartzee.code.screen.reporting

import burlton.core.code.util.Debug
import burlton.dartzee.code.bean.*
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.reporting.IncludedPlayerParameters
import burlton.dartzee.code.reporting.ReportParameters
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.PlayerSelectDialog
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.utils.getFilterPanel
import burlton.desktopcore.code.bean.DateFilterPanel
import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.createButtonGroup
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ReportingSetupScreen : EmbeddedScreen(), ChangeListener, ListSelectionListener
{
    private val hmIncludedPlayerToPanel = mutableMapOf<PlayerEntity, PlayerParametersPanel>()
    private val excludedPlayers = mutableListOf<PlayerEntity>()

    private val tabbedPane = JTabbedPane(SwingConstants.TOP)

    //Game tab
    private val panelGame = JPanel()
    private var panelGameParams: GameParamFilterPanel = GameParamFilterPanelX01()
    private val comboBox = ComboBoxGameType()
    private val cbStartDate = JCheckBox("Start Date")
    private val dateFilterPanelStart = DateFilterPanel()
    private val cbFinishDate = JCheckBox("Finish Date")
    private val rdbtnUnfinished = JRadioButton("")
    private val rdbtnDtFinish = JRadioButton("")
    private val dateFilterPanelFinish = DateFilterPanel()
    private val lblUnfinished = JLabel("Unfinished")
    private val panelUnfinishedLabel = JPanel()
    private val cbPartOfMatch = JCheckBox("Part of Match")
    private val rdbtnYes = JRadioButton("Yes")
    private val rdbtnNo = JRadioButton("No")

    //Included/Excluded Players
    private val panelIncludedPlayers = JPanel()
    private val panelExcludedPlayers = JPanel()
    private val panel_2 = JPanel()
    private val scrollTableExcluded = ScrollTable()
    private val panel_3 = JPanel()
    private val scrollTableIncluded = ScrollTable()
    private val btnAddIncluded = JButton("")
    private val btnRemoveIncluded = JButton("")
    private val btnAddExcluded = JButton("")
    private val btnRemoveExcluded = JButton("")
    private val defaultIncludedPlayerPanel = PlayerParametersPanel()

    //Will have one of these per included player, we'll swap them out/in as different players are selected
    private var includedPlayerPanel = defaultIncludedPlayerPanel
    private val checkBoxGameType = JCheckBox("Game")
    private val verticalStrut = Box.createVerticalStrut(20)
    private val horizontalStrut = Box.createHorizontalStrut(20)
    private val panelGameType = RadioButtonPanel()
    private val cbType = JCheckBox(" Type")

    init
    {
        add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addTab("Game", null, panelGame, null)
        panelGame.layout = MigLayout("hidemode 3", "[][][grow][]", "[][][][][][][][][][][][]")

        panelGame.add(checkBoxGameType, "flowx,cell 0 0")
        panelGame.add(horizontalStrut, "cell 1 0")
        panelGame.add(cbType, "cell 0 1")
        panelGame.add(panelGameParams, "cell 2 1")
        panelGame.add(verticalStrut, "cell 0 2")
        panelGame.add(cbStartDate, "cell 0 3,aligny center")
        val fl_panelDtStart = dateFilterPanelStart.layout as FlowLayout
        fl_panelDtStart.alignment = FlowLayout.LEFT
        panelGame.add(dateFilterPanelStart, "cell 2 3,alignx left,aligny center")
        panelGame.add(cbFinishDate, "cell 0 4")
        panelGame.add(rdbtnUnfinished, "flowx,cell 2 5")
        panelGame.add(rdbtnDtFinish, "flowx,cell 2 4")
        panelGame.add(dateFilterPanelFinish, "cell 2 4")
        panelGame.add(panelUnfinishedLabel, "cell 2 5")
        panelUnfinishedLabel.add(lblUnfinished)
        panelGame.add(cbPartOfMatch, "cell 0 6")
        panelGame.add(rdbtnYes, "flowx,cell 2 6")
        panelGame.add(panelGameType, "cell 2 0")
        panelGameType.add(comboBox)
        panelGame.add(rdbtnNo, "cell 2 6")
        tabbedPane.addTab("Players", null, panelIncludedPlayers, null)
        panelIncludedPlayers.layout = BorderLayout(0, 0)
        val flowLayout = panel_3.layout as FlowLayout
        flowLayout.alignment = FlowLayout.LEFT
        panelIncludedPlayers.add(panel_3, BorderLayout.NORTH)
        btnAddIncluded.preferredSize = Dimension(30, 30)
        btnAddIncluded.icon = ImageIcon(ReportingSetupScreen::class.java.getResource("/buttons/addPlayer.png"))
        panel_3.add(btnAddIncluded)
        btnRemoveIncluded.icon = ImageIcon(ReportingSetupScreen::class.java.getResource("/buttons/removePlayer.png"))
        btnRemoveIncluded.preferredSize = Dimension(30, 30)
        panel_3.add(btnRemoveIncluded)
        panelIncludedPlayers.add(scrollTableIncluded, BorderLayout.CENTER)
        panelIncludedPlayers.add(includedPlayerPanel, BorderLayout.SOUTH)
        tabbedPane.addTab("Excluded players", null, panelExcludedPlayers, null)
        panelExcludedPlayers.layout = BorderLayout(0, 0)
        val flowLayout_1 = panel_2.layout as FlowLayout
        flowLayout_1.alignment = FlowLayout.LEFT
        panelExcludedPlayers.add(panel_2, BorderLayout.NORTH)
        btnAddExcluded.icon = ImageIcon(ReportingSetupScreen::class.java.getResource("/buttons/addPlayer.png"))
        btnAddExcluded.preferredSize = Dimension(30, 30)
        panel_2.add(btnAddExcluded)
        btnRemoveExcluded.icon = ImageIcon(ReportingSetupScreen::class.java.getResource("/buttons/removePlayer.png"))
        btnRemoveExcluded.preferredSize = Dimension(30, 30)
        panel_2.add(btnRemoveExcluded)
        defaultIncludedPlayerPanel.disableAll()
        panelExcludedPlayers.add(scrollTableExcluded, BorderLayout.CENTER)

        createButtonGroupsAndSelectDefaults()
        addListeners()
    }


    override fun getScreenName() = "Report Setup"
    override fun initialise() {}

    private fun createButtonGroupsAndSelectDefaults()
    {
        createButtonGroup(rdbtnDtFinish, rdbtnUnfinished)
        createButtonGroup(rdbtnYes, rdbtnNo)
    }

    private fun addListeners()
    {
        comboBox.addActionListener(this)

        addChangeListener(checkBoxGameType)
        addChangeListener(cbType)
        addChangeListener(cbStartDate)
        addChangeListener(rdbtnDtFinish)
        addChangeListener(rdbtnUnfinished)
        addChangeListener(cbFinishDate)
        addChangeListener(cbPartOfMatch)

        btnAddIncluded.addActionListener(this)
        btnRemoveIncluded.addActionListener(this)
        btnAddExcluded.addActionListener(this)
        btnRemoveExcluded.addActionListener(this)

        val model = scrollTableIncluded.selectionModel
        model.addListSelectionListener(this)
    }

    private fun valid(): Boolean
    {
        if (!dateFilterPanelStart.valid())
        {
            return false
        }

        if (!dateFilterPanelFinish.valid())
        {
            return false
        }

        val players = hmIncludedPlayerToPanel.keys
        for (player in players)
        {
            val panel = hmIncludedPlayerToPanel[player]
            if (panel?.valid(player) != true)
            {
                return false
            }
        }

        return true
    }

    /**
     * Add the listener to the component and fire a state-changed to get its dependents in the correct state.
     */
    private fun addChangeListener(btn: AbstractButton)
    {
        btn.addChangeListener(this)
        stateChanged(ChangeEvent(btn))
    }

    override fun showNextButton() = true
    override fun nextPressed()
    {
        if (!valid())
        {
            return
        }

        val scrn = ScreenCache.getScreen(ReportingResultsScreen::class.java)

        val rp = generateReportParams()
        scrn.setReportParameters(rp)

        ScreenCache.switchScreen(scrn)
    }

    private fun generateReportParams(): ReportParameters
    {
        val rp = ReportParameters()

        if (checkBoxGameType.isSelected)
        {
            val gameType = comboBox.getGameType()
            rp.gameType = gameType
        }

        if (cbType.isSelected)
        {
            rp.gameParams = panelGameParams.getGameParams()
        }

        if (cbPartOfMatch.isSelected)
        {
            rp.setEnforceMatch(rdbtnYes.isSelected)
        }

        if (cbStartDate.isSelected)
        {
            val dtStartFrom = dateFilterPanelStart.sqlDtFrom
            val dtStartTo = dateFilterPanelStart.sqlDtTo

            rp.dtStartFrom = dtStartFrom
            rp.dtStartTo = dtStartTo
        }

        if (cbFinishDate.isSelected)
        {
            if (rdbtnUnfinished.isSelected)
            {
                rp.unfinishedOnly = true
            }
            else
            {
                val dtFinishFrom = dateFilterPanelFinish.sqlDtFrom
                val dtFinishTo = dateFilterPanelFinish.sqlDtTo

                rp.dtFinishFrom = dtFinishFrom
                rp.dtFinishTo = dtFinishTo
            }
        }

        val hmIncludedPlayerToParms = generateIncludedPlayerParams()
        rp.hmIncludedPlayerToParms = hmIncludedPlayerToParms
        rp.excludedPlayers = excludedPlayers

        return rp
    }

    private fun generateIncludedPlayerParams(): MutableMap<PlayerEntity, IncludedPlayerParameters>
    {
        val ret = mutableMapOf<PlayerEntity, IncludedPlayerParameters>()

        val includedPlayers = hmIncludedPlayerToPanel.keys
        for (player in includedPlayers)
        {
            val panel = hmIncludedPlayerToPanel[player]!!
            val parms = panel.generateParameters()

            ret[player] = parms
        }

        return ret
    }

    override fun stateChanged(arg0: ChangeEvent)
    {
        val src = arg0.source as AbstractButton
        val enabled = src.isSelected

        when (src)
        {
            checkBoxGameType -> comboBox.isEnabled = enabled
            cbType -> panelGameParams.isEnabled = enabled
            cbStartDate -> dateFilterPanelStart.enableComponents(enabled)
            cbFinishDate -> toggleDtFinishFilters(enabled)
            rdbtnUnfinished -> lblUnfinished.isEnabled = enabled
            rdbtnDtFinish -> dateFilterPanelFinish.enableComponents(enabled)
            cbPartOfMatch -> {rdbtnYes.isEnabled = enabled
                rdbtnNo.isEnabled = enabled}
            else -> Debug.stackTrace("Unexpected stateChanged [${src.text}]")
        }
    }

    private fun toggleDtFinishFilters(enabled: Boolean)
    {
        rdbtnDtFinish.isEnabled = enabled
        rdbtnUnfinished.isEnabled = enabled

        if (!enabled)
        {
            //CheckBox not enabled, so disable everything
            lblUnfinished.isEnabled = false
            dateFilterPanelFinish.enableComponents(false)
        }
        else
        {
            lblUnfinished.isEnabled = rdbtnUnfinished.isSelected
            dateFilterPanelFinish.enableComponents(rdbtnDtFinish.isSelected)
        }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            comboBox ->
            {
                panelGame.remove(panelGameParams)

                panelGameParams = getFilterPanel(comboBox.getGameType())

                panelGameParams.isEnabled = cbType.isSelected
                panelGame.add(panelGameParams, "cell 2 1")

                panelGame.revalidate()
            }

            btnAddIncluded -> addPlayers(scrollTableIncluded, hmIncludedPlayerToPanel.keys.toMutableList())
            btnAddExcluded -> addPlayers(scrollTableExcluded, excludedPlayers)
            btnRemoveIncluded -> removePlayers(scrollTableIncluded, hmIncludedPlayerToPanel.keys.toMutableList())
            btnRemoveExcluded -> removePlayers(scrollTableExcluded, excludedPlayers)
            else -> super.actionPerformed(arg0)
        }
    }

    private fun addPlayers(table: ScrollTable, tableList: MutableList<PlayerEntity>)
    {
        val allSelected = (hmIncludedPlayerToPanel.keys + excludedPlayers).toList()

        val players = PlayerSelectDialog.selectPlayers(allSelected)
        if (table === scrollTableIncluded)
        {
            for (player in players)
            {
                hmIncludedPlayerToPanel[player] = PlayerParametersPanel()
            }
        }

        tableList.addAll(players)
        table.initTableModel(tableList)
        table.selectFirstRow()
    }

    private fun removePlayers(table: ScrollTable, tableList: MutableList<PlayerEntity>)
    {
        val playersToRemove = table.getSelectedPlayers()
        if (playersToRemove.isEmpty())
        {
            DialogUtil.showError("You must select player(s) to remove.")
            return
        }

        tableList.removeAll(playersToRemove)
        table.initTableModel(tableList)

        //Bleh
        if (table === scrollTableIncluded)
        {
            for (player in playersToRemove)
            {
                hmIncludedPlayerToPanel.remove(player)
            }
        }
    }

    override fun valueChanged(arg0: ListSelectionEvent)
    {
        panelIncludedPlayers.remove(includedPlayerPanel)

        val player = scrollTableIncluded.getSelectedPlayer()
        if (player == null)
        {
            includedPlayerPanel = defaultIncludedPlayerPanel
        }
        else
        {
            includedPlayerPanel = hmIncludedPlayerToPanel[player]!!
        }

        panelIncludedPlayers.add(includedPlayerPanel, BorderLayout.SOUTH)

        ScreenCache.getMainScreen().pack()
        repaint()
    }
}
