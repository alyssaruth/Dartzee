package dartzee.screen.reporting

import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.isSelectedAndEnabled
import dartzee.core.util.addActionListenerToAllChildren
import dartzee.core.util.createButtonGroup
import dartzee.core.util.enableChildren
import dartzee.reporting.MatchFilter
import dartzee.reporting.ReportParametersGame
import dartzee.reporting.grabIfSelected
import dartzee.utils.getFilterPanel
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton
import net.miginfocom.swing.MigLayout

class ReportingGameTab : JPanel(), ActionListener {
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
    private val cbSyncStatus = JCheckBox("Sync Status")
    private val rdbtnSynced = JRadioButton("Synced")
    private val rdbtnPendingChanges = JRadioButton("Pending changes")

    init {
        layout = MigLayout("hidemode 3", "[][][grow][]", "[][][][][][][][][][][][]")

        add(checkBoxGameType, "flowx,cell 0 0")
        add(horizontalStrut, "cell 1 0")
        add(cbType, "cell 0 1")
        add(panelGameParams, "cell 2 1")
        add(verticalStrut, "cell 0 2")
        add(cbStartDate, "cell 0 3,aligny center")
        val fl = dateFilterPanelStart.layout as FlowLayout
        fl.alignment = FlowLayout.LEFT
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
        add(cbSyncStatus, "cell 0 7")
        add(rdbtnPendingChanges, "flowx,cell 2 7")
        add(rdbtnSynced, "cell 2 7")

        checkBoxGameType.name = "filterGameType"
        cbType.name = "filterGameParams"
        cbStartDate.name = "filterStartDate"
        cbFinishDate.name = "filterFinishDate"
        cbPartOfMatch.name = "filterPartOfMatch"
        cbSyncStatus.name = "filterSyncStatus"

        createButtonGroupsAndSelectDefaults()
        addListeners()
    }

    private fun createButtonGroupsAndSelectDefaults() {
        createButtonGroup(rdbtnDtFinish, rdbtnUnfinished)
        createButtonGroup(rdbtnYes, rdbtnNo)
        createButtonGroup(rdbtnPendingChanges, rdbtnSynced)
    }

    private fun addListeners() {
        addActionListenerToAllChildren(this)
        toggleComponents()
    }

    private fun toggleComponents() {
        comboBox.isEnabled = checkBoxGameType.isSelected
        panelGameParams.isEnabled = cbType.isSelected
        dateFilterPanelStart.enableChildren(cbStartDate.isSelected)

        rdbtnDtFinish.isEnabled = cbFinishDate.isSelected
        rdbtnUnfinished.isEnabled = cbFinishDate.isSelected
        dateFilterPanelFinish.enableChildren(rdbtnDtFinish.isSelectedAndEnabled())
        rdbtnYes.isEnabled = cbPartOfMatch.isSelected
        rdbtnNo.isEnabled = cbPartOfMatch.isSelected

        rdbtnPendingChanges.isEnabled = cbSyncStatus.isSelected
        rdbtnSynced.isEnabled = cbSyncStatus.isSelected
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            comboBox -> {
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

    fun generateReportParameters(): ReportParametersGame {
        val gameType = grabIfSelected(checkBoxGameType) { comboBox.getGameType() }
        val gameParams = grabIfSelected(cbType) { panelGameParams.getGameParams() }.orEmpty()
        val dtStartFrom = grabIfSelected(cbStartDate) { dateFilterPanelStart.getSqlDtFrom() }
        val dtStartTo = grabIfSelected(cbStartDate) { dateFilterPanelStart.getSqlDtTo() }
        val unfinishedOnly = grabIfSelected(cbFinishDate) { rdbtnUnfinished.isSelected } ?: false
        val dtFinishFrom =
            grabIfSelected(cbFinishDate) {
                if (rdbtnUnfinished.isSelected) null else dateFilterPanelFinish.getSqlDtFrom()
            }
        val dtFinishTo =
            grabIfSelected(cbFinishDate) {
                if (rdbtnUnfinished.isSelected) null else dateFilterPanelFinish.getSqlDtTo()
            }
        val enforceMatch = grabIfSelected(cbPartOfMatch) { enforceMatch() } ?: MatchFilter.BOTH
        val pendingChanges = grabIfSelected(cbSyncStatus) { rdbtnPendingChanges.isSelected }

        return ReportParametersGame(
            gameType,
            gameParams,
            dtStartFrom,
            dtStartTo,
            unfinishedOnly,
            dtFinishFrom,
            dtFinishTo,
            enforceMatch,
            pendingChanges
        )
    }

    private fun enforceMatch() =
        if (rdbtnYes.isSelected) MatchFilter.MATCHES_ONLY else MatchFilter.GAMES_ONLY
}
