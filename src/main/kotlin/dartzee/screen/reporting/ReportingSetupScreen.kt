package dartzee.screen.reporting

import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.util.createButtonGroup
import dartzee.core.util.enableChildren
import dartzee.logging.CODE_SWING_ERROR
import dartzee.reporting.ReportParameters
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.logger
import dartzee.utils.getFilterPanel
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class ReportingSetupScreen : EmbeddedScreen(), ChangeListener
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
        comboBox.addActionListener(this)

        addChangeListener(checkBoxGameType)
        addChangeListener(cbType)
        addChangeListener(cbStartDate)
        addChangeListener(rdbtnDtFinish)
        addChangeListener(rdbtnUnfinished)
        addChangeListener(cbFinishDate)
        addChangeListener(cbPartOfMatch)
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

    override fun stateChanged(arg0: ChangeEvent)
    {
        val src = arg0.source as AbstractButton
        val enabled = src.isSelected

        when (src)
        {
            checkBoxGameType -> comboBox.isEnabled = enabled
            cbType -> panelGameParams.isEnabled = enabled
            cbStartDate -> dateFilterPanelStart.enableChildren(enabled)
            cbFinishDate -> toggleDtFinishFilters(enabled)
            rdbtnUnfinished -> lblUnfinished.isEnabled = enabled
            rdbtnDtFinish -> dateFilterPanelFinish.enableChildren(enabled)
            cbPartOfMatch -> {rdbtnYes.isEnabled = enabled
                rdbtnNo.isEnabled = enabled}
            else -> logger.error(CODE_SWING_ERROR, "Unexpected stateChanged [${src.text}]")
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
            dateFilterPanelFinish.enableChildren(false)
        }
        else
        {
            lblUnfinished.isEnabled = rdbtnUnfinished.isSelected
            dateFilterPanelFinish.enableChildren(rdbtnDtFinish.isSelected)
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

            else -> super.actionPerformed(arg0)
        }
    }
}
