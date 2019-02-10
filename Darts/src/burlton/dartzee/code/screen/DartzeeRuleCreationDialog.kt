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

class DartzeeRuleCreationDialog : SimpleDialog()
{
    var dartzeeRule: DartzeeRuleEntity? = null

    private val panelCenter = JPanel()
    private val panelDarts = JPanel()
    private val rdbtnPanelDartScoreType = RadioButtonPanel()
    private val rdbtnAllDarts = JRadioButton("All Darts")
    private val dartOneSelector = DartzeeRuleSelector("Dart 1")
    private val dartTwoSelector = DartzeeRuleSelector("Dart 2")
    private val dartThreeSelector = DartzeeRuleSelector("Dart 3")
    private val cbInOrder = JCheckBox("In Order")
    private val targetSelector = DartzeeRuleSelector("Target")
    private val rdbtnAtLeastOne = JRadioButton("At least one dart")
    private val panelTotal = JPanel()

    init
    {
        title = "Add Dartzee Rule"
        setSize(400, 600)
        setLocationRelativeTo(ScreenCache.getMainScreen())
        isModal = true

        add(panelCenter, BorderLayout.CENTER)

        panelCenter.layout = MigLayout("", "[grow]", "[grow][grow][grow]")

        panelCenter.add(panelDarts, "cell 0 0,grow")

        panelDarts.layout = MigLayout("", "[][]", "[][][][]")
        rdbtnPanelDartScoreType.add(rdbtnAllDarts)
        rdbtnPanelDartScoreType.add(rdbtnAtLeastOne)
        panelDarts.add(rdbtnPanelDartScoreType, "spanx")

        panelCenter.add(panelTotal, "cell 0 1,grow")

        rdbtnPanelDartScoreType.addActionListener(this)

        toggleDartsComponents()
    }

    override fun actionPerformed(arg0: ActionEvent?)
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

        val rule = DartzeeRuleEntity()

        if (rdbtnAtLeastOne.isSelected)
        {
            rule.dart1Rule = targetSelector.getSelection()
        }
        else
        {
            rule.dart1Rule = dartOneSelector.getSelection()
            rule.dart2Rule = dartTwoSelector.getSelection()
            rule.dart3Rule = dartThreeSelector.getSelection()
            rule.inOrder = cbInOrder.isSelected
        }

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


    override fun cancelPressed()
    {
        dartzeeRule = null
        dispose()
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

        repaint()
    }

}