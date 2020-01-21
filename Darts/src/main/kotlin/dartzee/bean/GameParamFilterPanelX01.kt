package dartzee.bean

import java.awt.BorderLayout
import java.awt.event.ActionListener

import javax.swing.JPanel

class GameParamFilterPanelX01 : GameParamFilterPanel()
{
    private val panel = JPanel()
    val spinner = SpinnerX01()

    init
    {
        add(panel, BorderLayout.CENTER)
        panel.add(spinner)
    }

    override fun getGameParams(): String
    {
        return "${spinner.value}"
    }

    override fun setGameParams(gameParams: String)
    {
        spinner.value = gameParams.toInt()
    }

    override fun getFilterDesc(): String
    {
        return "games of ${getGameParams()}"
    }

    override fun enableChildren(enabled: Boolean)
    {
        spinner.isEnabled = enabled
    }

    override fun addActionListener(listener: ActionListener)
    {
        spinner.addActionListener(listener)
    }

    override fun removeActionListener(listener: ActionListener)
    {
        spinner.removeActionListener()
    }

}
