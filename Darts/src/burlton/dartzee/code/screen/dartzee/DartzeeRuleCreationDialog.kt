package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.bean.DartzeeRuleSelector
import burlton.dartzee.code.dartzee.ValidSegmentCalculationResult
import burlton.dartzee.code.dartzee.generateRuleDescription
import burlton.dartzee.code.db.DartzeeRuleEntity
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
    var dartzeeRule: DartzeeRuleEntity? = null

    val lblCombinations = JLabel()
    private val verificationPanel = DartzeeRuleVerificationPanel(this)
    private val panelCenter = JPanel()
    private val panelRuleStrength = JPanel()
    private val panelDarts = JPanel()
    private val rdbtnPanelDartScoreType = RadioButtonPanel()
    val rdbtnAllDarts = JRadioButton("All Darts")
    val dartOneSelector = DartzeeRuleSelector("Dart 1")
    val dartTwoSelector = DartzeeRuleSelector("Dart 2")
    val dartThreeSelector = DartzeeRuleSelector("Dart 3")
    val cbInOrder = JCheckBox("In Order")
    val targetSelector = DartzeeRuleSelector("Target")
    val rdbtnAtLeastOne = JRadioButton("At least one dart")
    val rdbtnNoDarts = JRadioButton("No darts")
    private val panelTotal = JPanel()
    private val panelAllowMisses = JPanel()
    val cbAllowMisses = JCheckBox("Allow misses")
    val totalSelector = DartzeeRuleSelector("Total", true, true)
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

        panelRuleName.layout = BorderLayout(0, 0)
        panelRuleName.border = TitledBorder("")
        panelRuleName.add(tfName, BorderLayout.CENTER)
        tfName.preferredSize = Dimension(30, 50)

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

    fun populate(rule: DartzeeRuleEntity)
    {
        this.dartzeeRule = rule
        title = "Amend Dartzee Rule"

        if (rule.dart1Rule.isEmpty())
        {
            rdbtnNoDarts.isSelected = true
        }
        else if (rule.dart2Rule.isEmpty())
        {
            rdbtnAtLeastOne.isSelected = true

            targetSelector.populate(rule.dart1Rule)
        }
        else
        {
            cbInOrder.isSelected = rule.inOrder

            dartOneSelector.populate(rule.dart1Rule)
            dartTwoSelector.populate(rule.dart2Rule)
            dartThreeSelector.populate(rule.dart3Rule)
        }

        if (!rule.totalRule.isEmpty())
        {
            totalSelector.populate(rule.totalRule)
        }

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

        val rule = dartzeeRule ?: DartzeeRuleEntity()

        populateRuleFromComponents(rule)

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

    private fun populateRuleFromComponents(rule: DartzeeRuleEntity)
    {
        if (rdbtnAllDarts.isSelected)
        {
            rule.dart1Rule = dartOneSelector.getSelection().toDbString()
            rule.dart2Rule = dartTwoSelector.getSelection().toDbString()
            rule.dart3Rule = dartThreeSelector.getSelection().toDbString()
            rule.inOrder = cbInOrder.isSelected
        }
        else
        {
            rule.dart1Rule = if (rdbtnAtLeastOne.isSelected) targetSelector.getSelection().toDbString() else ""
            rule.dart2Rule = ""
            rule.dart3Rule = ""
        }

        if (totalSelector.isEnabled)
        {
            rule.totalRule = totalSelector.getSelection().toDbString()
        }
        else
        {
            rule.totalRule = ""
        }

        rule.allowMisses = cbAllowMisses.isSelected
    }

    private fun valid(): Boolean
    {
        val valid = if (rdbtnAtLeastOne.isSelected)
        {
            targetSelector.valid()
        }
        else
        {
            dartOneSelector.valid() && dartTwoSelector.valid() && dartThreeSelector.valid()
        }

        if (!valid)
        {
            return false
        }

        return true
    }

    fun updateRuleStrength(calculationResult: ValidSegmentCalculationResult)
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
            val rule = DartzeeRuleEntity().also { populateRuleFromComponents(it) }
            val ruleName = rule.generateRuleDescription()
            tfName.text = ruleName

            verificationPanel.updateRule(rule)
        }
    }
}