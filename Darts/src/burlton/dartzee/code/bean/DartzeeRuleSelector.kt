package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.getAllDartRules
import java.awt.FlowLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DartzeeRuleSelector(desc: String): JPanel()
{
    private val lblDesc = JLabel(desc)
    private val comboBoxRuleType = JComboBox<AbstractDartzeeDartRule>()

    init
    {
        layout = FlowLayout()

        populateComboBox()

        add(lblDesc)
        add(comboBoxRuleType)
    }

    private fun populateComboBox()
    {
        val rules = getAllDartRules()

        val model = DefaultComboBoxModel<AbstractDartzeeDartRule>()

        rules.forEach{
            model.addElement(it)
        }

        comboBoxRuleType.model = model
    }

    fun getSelection(): AbstractDartzeeDartRule
    {
        val ix = comboBoxRuleType.selectedIndex
        return comboBoxRuleType.getItemAt(ix)
    }
}