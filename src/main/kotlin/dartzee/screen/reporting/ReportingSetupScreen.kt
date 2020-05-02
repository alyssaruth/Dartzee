package dartzee.screen.reporting

import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.bean.isSelectedAndEnabled
import dartzee.core.util.addActionListenerToAllChildren
import dartzee.core.util.createButtonGroup
import dartzee.core.util.enableChildren
import dartzee.reporting.ReportParameters
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.utils.getFilterPanel
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.*

class ReportingSetupScreen: EmbeddedScreen()
{
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)

    //Game tab
    private val checkBoxGameType = JCheckBox("Game")
    private val verticalStrut = Box.createVerticalStrut(20)
    private val horizontalStrut = Box.createHorizontalStrut(20)
    private val panelGameType = RadioButtonPanel()
    private val cbType = JCheckBox(" Type")
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

    //Player tab
    private val playerTab = ReportingPlayersTab()

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
        tabbedPane.addTab("Players", null, playerTab, null)

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
        addActionListenerToAllChildren(this)
        toggleComponents()
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

        return playerTab.valid()
    }

    override fun showNextButton() = true
    override fun nextPressed()
    {
        if (!valid())
        {
            return
        }

        val scrn = ScreenCache.get<ReportingResultsScreen>()

        val rp = generateReportParams()
        scrn.setReportParameters(rp)

        ScreenCache.switch(scrn)
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
            rp.dtStartFrom = dateFilterPanelStart.getSqlDtFrom()
            rp.dtStartTo = dateFilterPanelStart.getSqlDtTo()
        }

        if (cbFinishDate.isSelected)
        {
            if (rdbtnUnfinished.isSelected)
            {
                rp.unfinishedOnly = true
            }
            else
            {
                rp.dtFinishFrom = dateFilterPanelFinish.getSqlDtFrom()
                rp.dtFinishTo = dateFilterPanelFinish.getSqlDtTo()
            }
        }

        playerTab.populateReportParameters(rp)

        return rp
    }

    private fun toggleComponents()
    {
        comboBox.isEnabled = checkBoxGameType.isSelected
        panelGameParams.isEnabled = cbType.isSelected
        dateFilterPanelStart.enableChildren(cbStartDate.isSelected)

        rdbtnDtFinish.isEnabled = cbFinishDate.isSelected
        rdbtnUnfinished.isEnabled = cbFinishDate.isSelected
        lblUnfinished.isEnabled = rdbtnUnfinished.isSelectedAndEnabled()
        dateFilterPanelFinish.enableChildren(rdbtnDtFinish.isSelectedAndEnabled())
        rdbtnYes.isEnabled = cbPartOfMatch.isSelected
        rdbtnNo.isEnabled = cbPartOfMatch.isSelected
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
            else -> toggleComponents()
        }
    }
}
