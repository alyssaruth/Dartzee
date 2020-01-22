package dartzee.core.bean

import java.awt.Dimension

import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBoxNumberComparison : JComboBox<String>()
{
    private val filterModes = arrayOf(FILTER_MODE_EQUAL_TO, FILTER_MODE_GREATER_THAN, FILTER_MODE_LESS_THAN)
    private val comboModel = DefaultComboBoxModel(filterModes)

    init
    {
        model = comboModel
        preferredSize = Dimension(40, 30)
        maximumSize = Dimension(40, 30)
    }

    fun addOption(option: String)
    {
        comboModel.addElement(option)
    }

    companion object
    {
        const val FILTER_MODE_EQUAL_TO = "="
        const val FILTER_MODE_GREATER_THAN = ">"
        const val FILTER_MODE_LESS_THAN = "<"
    }
}
