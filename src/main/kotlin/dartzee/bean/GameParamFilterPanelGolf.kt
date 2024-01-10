package dartzee.bean

import dartzee.core.bean.RadioButtonPanel
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JRadioButton

class GameParamFilterPanelGolf : GameParamFilterPanel() {
    private val panel = RadioButtonPanel()
    val rdbtn9 = JRadioButton("9 holes")
    val rdbtn18 = JRadioButton("18 holes")

    init {

        add(panel, BorderLayout.CENTER)
        panel.add(rdbtn9)
        panel.add(rdbtn18)

        rdbtn18.isSelected = true // Default to 18
    }

    override fun getGameParams(): String {
        val selection = panel.getSelectionStr()
        return selection.replace(" holes", "")
    }

    override fun setGameParams(gameParams: String) {
        if (gameParams == "9") {
            rdbtn9.isSelected = true
        } else {
            rdbtn18.isSelected = true
        }
    }

    override fun getFilterDesc() = "games of ${panel.getSelectionStr()}"

    override fun enableChildren(enabled: Boolean) {
        rdbtn9.isEnabled = enabled
        rdbtn18.isEnabled = enabled
    }

    override fun addActionListener(listener: ActionListener) {
        panel.addActionListener(listener)
    }

    override fun removeActionListener(listener: ActionListener) {
        panel.removeActionListener(listener)
    }
}
