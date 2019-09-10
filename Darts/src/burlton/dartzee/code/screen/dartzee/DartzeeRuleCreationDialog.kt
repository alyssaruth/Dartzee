package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.bean.DartzeeDartRuleSelector
import burlton.dartzee.code.bean.DartzeeTotalRuleSelector
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.ValidSegmentCalculationResult
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.setFontSize
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartzeeRuleCreationDialog : SimpleDialog(), ChangeListener
{
    var dartzeeRule: DartzeeRuleDto? = null

    private val verificationPanel = DartzeeRuleVerificationPanel(this)
    val lblCombinations = JLabel()
    private val panelCenter = JPanel()
    private val panelRuleStrength = JPanel()
    private val panelDarts = JPanel()
    private val rdbtnPanelDartScoreType = RadioButtonPanel()
    val rdbtnAllDarts = JRadioButton("All Darts")
    val dartOneSelector = DartzeeDartRuleSelector("Dart 1")
    val dartTwoSelector = DartzeeDartRuleSelector("Dart 2")
    val dartThreeSelector = DartzeeDartRuleSelector("Dart 3")
    val cbInOrder = JCheckBox("In Order")
    val targetSelector = DartzeeDartRuleSelector("Target")
    val rdbtnAtLeastOne = JRadioButton("At least one dart")
    val rdbtnNoDarts = JRadioButton("No darts")
    private val panelTotal = JPanel()
    private val panelAllowMisses = JPanel()
    val cbAllowMisses = JCheckBox("Allow misses")
    val totalSelector = DartzeeTotalRuleSelector("Total")
    private val panelRuleName = JPanel()
    val tfName = JTextField()

    init
    {
        title = "Add Dartzee Rule"
        setSize(900, 700)
        setLocationRelativeTo(ScreenCache.getMainScreen())
        isModal = true

        add(panelRuleName, BorderLayout.NORTH)
        add(panelCenter, BorderLayout.CENTER)
        add(verificationPanel, BorderLayout.EAST)

        lblCombinations.setFontSize(24)
        panelRuleStrength.add(lblCombinations)

        panelCenter.layout = MigLayout("", "[grow]", "[grow][grow][grow]")
        panelCenter.add(panelRuleStrength, "cell 0 1, growx")
        panelCenter.add(panelDarts, "cell 0 2, growx")
        panelCenter.add(panelTotal, "cell 0 3, growx")
        panelCenter.add(panelAllowMisses, "cell 0 4, growx")

        panelRuleStrength.border = TitledBorder("")
        panelDarts.border = TitledBorder("")
        panelDarts.layout = MigLayout("", "[][]", "[][][][]")
        rdbtnPanelDartScoreType.add(rdbtnAllDarts)
        rdbtnPanelDartScoreType.add(rdbtnAtLeastOne)
        rdbtnPanelDartScoreType.add(rdbtnNoDarts)
        panelDarts.add(rdbtnPanelDartScoreType, "spanx")
        panelDarts.validate()

        panelTotal.layout = MigLayout("", "[]", "[]")
        panelTotal.border = TitledBorder("")

        panelTotal.add(totalSelector, "cell 0 0")

        panelAllowMisses.layout = MigLayout("", "[]", "[]")
        panelAllowMisses.border = TitledBorder("")
        panelAllowMisses.add(cbAllowMisses, "cell 0 0")

        panelRuleName.layout = BorderLayout(0,0)
        panelRuleName.border = TitledBorder("")
        panelRuleName.add(tfName, BorderLayout.CENTER)
        tfName.preferredSize = Dimension(900, 50)

        tfName.horizontalAlignment = JTextField.CENTER
        tfName.setFontSize(24)
        tfName.isEditable = false

        rdbtnPanelDartScoreType.addActionListener(this)
        dartOneSelector.addActionListener(this)
        dartTwoSelector.addActionListener(this)
        dartThreeSelector.addActionListener(this)
        targetSelector.addActionListener(this)
        totalSelector.addActionListener(this)
        cbInOrder.addActionListener(this)
        cbAllowMisses.addActionListener(this)

        cbInOrder.isSelected = true

        updateComponents()
    }

    fun populate(rule: DartzeeRuleDto)
    {
        this.dartzeeRule = rule
        title = "Amend Dartzee Rule"

        if (rule.dart1Rule == null)
        {
            rdbtnNoDarts.isSelected = true
        }
        else if (rule.dart2Rule == null)
        {
            rdbtnAtLeastOne.isSelected = true

            targetSelector.populate(rule.dart1Rule)
        }
        else
        {
            cbInOrder.isSelected = rule.inOrder

            dartOneSelector.populate(rule.dart1Rule)
            dartTwoSelector.populate(rule.dart2Rule)
            dartThreeSelector.populate(rule.dart3Rule!!)
        }

        rule.totalRule?.let { totalSelector.populate(rule.totalRule) }

        cbAllowMisses.isSelected = rule.allowMisses

        updateComponents()
        repaint()
    }

    override fun stateChanged(e: ChangeEvent?)
    {
        updateComponents()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (arg0.source !in listOf(btnOk, btnCancel))
        {
            updateComponents()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    override fun okPressed()
    {
        if (!valid())
        {
            return
        }

        val rule = constructRuleFromComponents()

        val calculationResult = rule.runStrengthCalculation(verificationPanel.dartboard)
        val combinations = calculationResult.validCombinations
        if (combinations == 0)
        {
            DialogUtil.showError("This rule is impossible!")
            return
        }

        dartzeeRule = rule

        dispose()
    }

    private fun constructRuleFromComponents(): DartzeeRuleDto
    {
        val totalRule = if (totalSelector.isEnabled) totalSelector.getSelection() else null

        return if (rdbtnAllDarts.isSelected)
        {
            DartzeeRuleDto(dartOneSelector.getSelection(),
                    dartTwoSelector.getSelection(), dartThreeSelector.getSelection(), totalRule, cbInOrder.isSelected, cbAllowMisses.isSelected)
        }
        else
        {
            val dart1Rule = if (rdbtnAtLeastOne.isSelected) targetSelector.getSelection() else null
            DartzeeRuleDto(dart1Rule, null, null, totalRule, false, cbAllowMisses.isSelected)
        }
    }

    private fun valid(): Boolean
    {
        return if (rdbtnAtLeastOne.isSelected)
        {
            targetSelector.valid()
        }
        else
        {
            dartOneSelector.valid() && dartTwoSelector.valid() && dartThreeSelector.valid()
        }
    }

    fun updateResults(calculationResult: ValidSegmentCalculationResult)
    {
        lblCombinations.text = calculationResult.getCombinationsDesc()
        lblCombinations.repaint()
    }

    private fun updateComponents()
    {
        if (rdbtnAllDarts.isSelected)
        {
            panelDarts.remove(targetSelector)
            panelDarts.add(dartOneSelector, "cell 0 1")
            panelDarts.add(dartTwoSelector, "cell 0 2")
            panelDarts.add(dartThreeSelector, "cell 0 3")
            panelDarts.add(cbInOrder, "cell 0 4")
        }
        else
        {
            panelDarts.remove(dartOneSelector)
            panelDarts.remove(dartTwoSelector)
            panelDarts.remove(dartThreeSelector)
            panelDarts.remove(cbInOrder)

            if (rdbtnAtLeastOne.isSelected)
            {
                panelDarts.add(targetSelector, "cell 0 1")
            }
            else
            {
                panelDarts.remove(targetSelector)
            }
        }

        repaint()
        panelDarts.revalidate()

        SwingUtilities.invokeLater{
            val rule = constructRuleFromComponents()
            val ruleName = rule.generateRuleDescription()
            tfName.text = ruleName

            verificationPanel.updateRule(rule)
        }
    }
}