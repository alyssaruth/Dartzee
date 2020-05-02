package dartzee.screen.reporting

import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.isSelectedAndEnabled
import dartzee.core.util.addActionListenerToAllChildren
import dartzee.core.util.createButtonGroup
import dartzee.core.util.enableChildren
import dartzee.reporting.ReportParameters
import dartzee.utils.getFilterPanel
import net.miginfocom.swing.MigLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton

class ReportingGameTab: JPanel(), ActionListener
{
    private val checkBoxGameType = JCheckBox("Game")
    private val verticalStrut = Box.createVerticalStrut(20)
    private val horizontalStrut = Box.createHorizontalStrut(20)
    private val panelGameType = JPanel()
    private val cbType = JCheckBox("Type")
    private var panelGameParams: GameParamFilterPanel = GameParamFilterPanelX01()
    private val comboBox = ComboBoxGameType()
    private val cbStartDate = JCheckBox("Start Date")
    private val dateFilterPanelStart = DateFilterPanel()
    private val cbFinishDate = JCheckBox("Finish Date")
    private val rdbtnUnfinished = JRadioButton("Unfinished")
    private val rdbtnDtFinish = JRadioButton("Finished:")
    private val dateFilterPanelFinish = DateFilterPanel()
    private val panelUnfinishedLabel = JPanel()
    private val cbPartOfMatch = JCheckBox("Part of Match")
    private val rdbtnYes = JRadioButton("Yes")
    private val rdbtnNo = JRadioButton("No")

    init
    {
        layout = MigLayout("hidemode 3", "[][][grow][]", "[][][][][][][][][][][][]")

        add(checkBoxGameType, "flowx,cell 0 0")
        add(horizontalStrut, "cell 1 0")
        add(cbType, "cell 0 1")
        add(panelGameParams, "cell 2 1")
        add(verticalStrut, "cell 0 2")
        add(cbStartDate, "cell 0 3,aligny center")
        val fl_panelDtStart = dateFilterPanelStart.layout as FlowLayout
        fl_panelDtStart.alignment = FlowLayout.LEFT
        add(dateFilterPanelStart, "cell 2 3,alignx left,aligny center")
        add(cbFinishDate, "cell 0 4")
        add(rdbtnUnfinished, "flowx,cell 2 5")
        add(rdbtnDtFinish, "flowx,cell 2 4")
        add(dateFilterPanelFinish, "cell 2 4")
        add(panelUnfinishedLabel, "cell 2 5")
        add(cbPartOfMatch, "cell 0 6")
        add(rdbtnYes, "flowx,cell 2 6")
        add(panelGameType, "cell 2 0")
        panelGameType.add(comboBox)
        add(rdbtnNo, "cell 2 6")

        createButtonGroupsAndSelectDefaults()
        addListeners()
    }

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

    private fun toggleComponents()
    {
        comboBox.isEnabled = checkBoxGameType.isSelected
        panelGameParams.isEnabled = cbType.isSelected
        dateFilterPanelStart.enableChildren(cbStartDate.isSelected)

        rdbtnDtFinish.isEnabled = cbFinishDate.isSelected
        rdbtnUnfinished.isEnabled = cbFinishDate.isSelected
        dateFilterPanelFinish.enableChildren(rdbtnDtFinish.isSelectedAndEnabled())
        rdbtnYes.isEnabled = cbPartOfMatch.isSelected
        rdbtnNo.isEnabled = cbPartOfMatch.isSelected
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        when (e?.source)
        {
            comboBox ->
            {
                remove(panelGameParams)
                panelGameParams = getFilterPanel(comboBox.getGameType())
                panelGameParams.isEnabled = cbType.isSelected
                add(panelGameParams, "cell 2 1")
                revalidate()
            }
            else -> toggleComponents()
        }
    }

    fun valid() = dateFilterPanelStart.valid() && dateFilterPanelFinish.valid()

    fun populateReportParameters(rp: ReportParameters)
    {
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
    }
}