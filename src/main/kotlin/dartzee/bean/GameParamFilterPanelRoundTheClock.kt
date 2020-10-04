package dartzee.bean

import dartzee.core.bean.RadioButtonPanel
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JRadioButton

class GameParamFilterPanelRoundTheClock : GameParamFilterPanel()
{
    private val panel = RadioButtonPanel()
    private val rdbtnStandard = JRadioButton("${ClockType.Standard}")
    private val rdbtnDoubles = JRadioButton("${ClockType.Doubles}")
    private val rdbtnTrebles = JRadioButton("${ClockType.Trebles}")
    private val separator = Box.createHorizontalStrut(20)
    private val checkBoxInOrder = JCheckBox("In order")

    init
    {
        checkBoxInOrder.isSelected = true

        add(panel, BorderLayout.CENTER)
        panel.add(checkBoxInOrder)
        panel.add(separator)
        panel.add(rdbtnStandard)
        panel.add(rdbtnDoubles)
        panel.add(rdbtnTrebles)

    }

    override fun getGameParams() = getConfigFromSelection().toJson()

    private fun getConfigFromSelection(): RoundTheClockConfig
    {
        val selectedType = panel.getSelectionStr()
        val clockType = ClockType.valueOf(selectedType)
        return RoundTheClockConfig(clockType, checkBoxInOrder.isSelected)
    }

    override fun setGameParams(gameParams: String)
    {
        val config = RoundTheClockConfig.fromJson(gameParams)
        checkBoxInOrder.isSelected = config.inOrder
        panel.setSelection(config.clockType.toString())
    }

    override fun getFilterDesc(): String
    {
        val config = getConfigFromSelection()
        return "${config.clockType} games (${config.getOrderStr()})"
    }

    override fun enableChildren(enabled: Boolean)
    {
        rdbtnStandard.isEnabled = enabled
        rdbtnDoubles.isEnabled = enabled
        rdbtnTrebles.isEnabled = enabled
        checkBoxInOrder.isEnabled = enabled
    }

    override fun addActionListener(listener: ActionListener)
    {
        panel.addActionListener(listener)
        checkBoxInOrder.addActionListener(listener)
    }

    override fun removeActionListener(listener: ActionListener)
    {
        panel.removeActionListener(listener)
        checkBoxInOrder.removeActionListener(listener)
    }
}
