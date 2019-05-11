package burlton.dartzee.code.bean

import burlton.desktopcore.code.bean.RadioButtonPanel
import javax.swing.JRadioButton

class PlayerTypeFilterPanel : RadioButtonPanel()
{
    val rdbtnAll = JRadioButton("All")
    val rdbtnHuman = JRadioButton("Human")
    val rdbtnAi = JRadioButton("AI")

    init
    {
        add(rdbtnAll)
        add(rdbtnHuman)
        add(rdbtnAi)
    }

    fun getWhereSql(): String
    {
        return when
        {
            rdbtnHuman.isSelected -> "Strategy = -1"
            rdbtnAi.isSelected -> "Strategy > -1"
            else -> ""
        }
    }
}
