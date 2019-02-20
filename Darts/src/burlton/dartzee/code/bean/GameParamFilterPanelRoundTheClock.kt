package burlton.dartzee.code.bean

import burlton.dartzee.code.db.CLOCK_TYPE_DOUBLES
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.CLOCK_TYPE_TREBLES
import burlton.desktopcore.code.bean.RadioButtonPanel
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JRadioButton

class GameParamFilterPanelRoundTheClock : GameParamFilterPanel()
{
    private val panel = RadioButtonPanel()
    private val rdbtnStandard = JRadioButton(CLOCK_TYPE_STANDARD)
    private val rdbtnDoubles = JRadioButton(CLOCK_TYPE_DOUBLES)
    private val rdbtnTrebles = JRadioButton(CLOCK_TYPE_TREBLES)

    init
    {

        add(panel, BorderLayout.CENTER)
        panel.add(rdbtnStandard)
        panel.add(rdbtnDoubles)
        panel.add(rdbtnTrebles)
    }

    override fun getGameParams(): String
    {
        return panel.selectionStr
    }

    override fun setGameParams(gameParams: String)
    {
        panel.setSelection(gameParams)
    }

    override fun getFilterDesc(): String
    {
        return "${getGameParams()} games"
    }

    override fun enableChildren(enabled: Boolean)
    {
        rdbtnStandard.setEnabled(enabled)
        rdbtnDoubles.setEnabled(enabled)
        rdbtnTrebles.setEnabled(enabled)
    }

    override fun addActionListener(listener: ActionListener)
    {
        panel.addActionListener(listener)
    }
}
