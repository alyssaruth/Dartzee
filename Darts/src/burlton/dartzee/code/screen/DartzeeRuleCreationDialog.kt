package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.DartzeeRuleSelector
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.TitledBorder

class DartzeeRuleCreationDialog : SimpleDialog()
{
    var dartzeeRule: DartzeeRuleEntity? = null

    private val panelCenter = JPanel()
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
    val cbTotal = JCheckBox("")
    val totalSelector = DartzeeRuleSelector("Total", true)
    private val panelRuleName = JPanel()
    val tfName = JTextField()
    val btnGenerateName = JButton()

    init
    {
        title = "Add Dartzee Rule"
        setSize(450, 600)
        setLocationRelativeTo(ScreenCache.getMainScreen())
        isModal = true

        add(panelCenter, BorderLayout.CENTER)

        panelCenter.layout = MigLayout("", "[grow]", "[grow][grow][grow]")
        panelCenter.add(panelRuleName, "cell 0 0, growx")
        panelCenter.add(panelDarts, "cell 0 1, growx")
        panelCenter.add(panelTotal, "cell 0 2, growx")

        panelDarts.border = TitledBorder("")
        panelDarts.layout = MigLayout("", "[][]", "[][][][]")
        rdbtnPanelDartScoreType.add(rdbtnAllDarts)
        rdbtnPanelDartScoreType.add(rdbtnAtLeastOne)
        rdbtnPanelDartScoreType.add(rdbtnNoDarts)
        panelDarts.add(rdbtnPanelDartScoreType, "spanx")
        panelDarts.validate()

        panelTotal.layout = MigLayout("", "[]", "[]")
        panelTotal.border = TitledBorder("")

        panelTotal.add(cbTotal, "cell 0 0")
        panelTotal.add(totalSelector, "cell 1 0")

        panelRuleName.layout = BorderLayout(0, 0)
        panelRuleName.border = TitledBorder("")
        panelRuleName.add(tfName, BorderLayout.CENTER)
        panelRuleName.add(btnGenerateName, BorderLayout.EAST)
        tfName.preferredSize = Dimension(30, 40)
        btnGenerateName.preferredSize = Dimension(40, 40)

        cbTotal.addActionListener(this)
        rdbtnPanelDartScoreType.addActionListener(this)
        dartOneSelector.addActionListener(this)
        dartTwoSelector.addActionListener(this)
        dartThreeSelector.addActionListener(this)
        targetSelector.addActionListener(this)
        totalSelector.addActionListener(this)
        cbInOrder.addActionListener(this)

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
            cbTotal.isSelected = true
            totalSelector.populate(rule.totalRule)
        }

        updateComponents()
        repaint()
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

        if (cbTotal.isSelected)
        {
            rule.totalRule = totalSelector.getSelection().toDbString()
        }
    }

    private fun valid(): Boolean
    {
        if (rdbtnNoDarts.isSelected && !cbTotal.isSelected)
        {
            DialogUtil.showError("You cannot create an empty rule")
            return false
        }

        if (rdbtnAtLeastOne.isSelected)
        {
            return targetSelector.valid()
        }
        else
        {
            return dartOneSelector.valid() && dartTwoSelector.valid() && dartThreeSelector.valid()
        }
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

        totalSelector.isEnabled = cbTotal.isSelected

        repaint()
        panelDarts.revalidate()

        SwingUtilities.invokeLater{
            val rule = DartzeeRuleEntity().also { populateRuleFromComponents(it) }
            val ruleName = rule.generateRuleDescription()
            tfName.text = ruleName
        }
    }

}