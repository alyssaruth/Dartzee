package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.DartzeeRuleSelector
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.code.screen.SimpleDialog
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JRadioButton
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
    private val panelTotal = JPanel()
    val cbTotal = JCheckBox("")
    val totalSelector = DartzeeRuleSelector("Total", true)

    init
    {
        title = "Add Dartzee Rule"
        setSize(450, 600)
        setLocationRelativeTo(ScreenCache.getMainScreen())
        isModal = true

        add(panelCenter, BorderLayout.CENTER)

        panelCenter.layout = MigLayout("", "[grow]", "[grow][grow][grow]")

        panelCenter.add(panelDarts, "cell 0 0,grow")
        panelDarts.border = TitledBorder("")
        panelDarts.layout = MigLayout("", "[][]", "[][][][]")
        rdbtnPanelDartScoreType.add(rdbtnAllDarts)
        rdbtnPanelDartScoreType.add(rdbtnAtLeastOne)
        panelDarts.add(rdbtnPanelDartScoreType, "spanx")

        panelTotal.border = TitledBorder("")
        panelCenter.add(panelTotal, "cell 0 1,grow")
        panelTotal.add(cbTotal)
        panelTotal.add(totalSelector)

        rdbtnPanelDartScoreType.addActionListener(this)

        toggleDartsComponents()
    }

    fun populate(rule: DartzeeRuleEntity)
    {
        this.dartzeeRule = rule
        title = "Amend Dartzee Rule"

        if (rule.dart2Rule.isEmpty())
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

        toggleDartsComponents()
        repaint()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (rdbtnPanelDartScoreType.isEventSource(arg0))
        {
            toggleDartsComponents()
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

        if (rdbtnAtLeastOne.isSelected)
        {
            rule.dart1Rule = targetSelector.getSelection().toDbString()
            rule.dart2Rule = ""
            rule.dart3Rule = ""
        }
        else
        {
            rule.dart1Rule = dartOneSelector.getSelection().toDbString()
            rule.dart2Rule = dartTwoSelector.getSelection().toDbString()
            rule.dart3Rule = dartThreeSelector.getSelection().toDbString()
            rule.inOrder = cbInOrder.isSelected
        }

        dartzeeRule = rule

        dispose()
    }

    private fun valid(): Boolean
    {
        if (rdbtnAtLeastOne.isSelected)
        {
            return targetSelector.valid()
        }
        else
        {
            return dartOneSelector.valid() && dartTwoSelector.valid() && dartThreeSelector.valid()
        }
    }

    private fun toggleDartsComponents()
    {
        if (rdbtnAtLeastOne.isSelected)
        {
            panelDarts.add(targetSelector, "cell 0 1")
            panelDarts.remove(dartOneSelector)
            panelDarts.remove(dartTwoSelector)
            panelDarts.remove(dartThreeSelector)
            panelDarts.remove(cbInOrder)
        }
        else
        {
            panelDarts.remove(targetSelector)
            panelDarts.add(dartOneSelector, "cell 0 1")
            panelDarts.add(dartTwoSelector, "cell 0 2")
            panelDarts.add(dartThreeSelector, "cell 0 3")
            panelDarts.add(cbInOrder, "cell 0 4")
        }

        totalSelector.isEnabled = cbTotal.isSelected

        repaint()
    }

}