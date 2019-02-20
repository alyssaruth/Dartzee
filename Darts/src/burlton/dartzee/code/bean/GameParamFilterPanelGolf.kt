package burlton.dartzee.code.bean

import burlton.desktopcore.code.bean.RadioButtonPanel
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JRadioButton

class GameParamFilterPanelGolf : GameParamFilterPanel()
{
    private val panel = RadioButtonPanel()
    private val rdbtn9 = JRadioButton("9 holes")
    private val rdbtn18 = JRadioButton("18 holes")

    init
    {

        add(panel, BorderLayout.CENTER)
        panel.add(rdbtn9)
        panel.add(rdbtn18)

        rdbtn18.isSelected = true //Default to 18
    }

    override fun getGameParams(): String
    {
        val selection = panel.selectionStr
        return selection.replace(" holes", "")
    }

    override fun setGameParams(gameParams: String)
    {
        if (gameParams == "9")
        {
            rdbtn9.isSelected = true
        }
        else
        {
            rdbtn18.isSelected = true
        }
    }

    override fun getFilterDesc(): String
    {
        return "games of ${panel.selectionStr}"
    }

    override fun enableChildren(enabled: Boolean)
    {
        rdbtn9.isEnabled = enabled
        rdbtn18.isEnabled = enabled
    }

    override fun addActionListener(listener: ActionListener)
    {
        panel.addActionListener(listener)
    }
}
