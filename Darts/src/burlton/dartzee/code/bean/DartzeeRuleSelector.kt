package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import java.awt.FlowLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DartzeeRuleSelector(desc: String): JPanel()
{
    private val lblDesc = JLabel(desc)
    private val comboBoxRuleType = JComboBox<AbstractDartzeeRule>()

    init
    {
        layout = FlowLayout()

        add(lblDesc)
        add(comboBoxRuleType)
    }
}