package dartzee.bean

import dartzee.game.FinishType
import dartzee.game.X01Config
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JPanel

class GameParamFilterPanelX01 : GameParamFilterPanel() {
    private val panel = JPanel()
    private val spinner = SpinnerX01()
    private val cbFinishOnDouble = JCheckBox("Finish on double")
    private val separator = Box.createHorizontalStrut(20)

    init {
        add(panel, BorderLayout.CENTER)
        panel.add(cbFinishOnDouble)
        panel.add(separator)
        panel.add(spinner)
        cbFinishOnDouble.isSelected = true
    }

    override fun getGameParams() = constructGameParams().toJson()

    override fun setGameParams(gameParams: String) {
        val config = X01Config.fromJson(gameParams)
        spinner.value = config.target
        cbFinishOnDouble.isSelected = config.finishType == FinishType.Doubles
    }

    private fun constructGameParams() =
        X01Config(
            spinner.value as Int,
            if (cbFinishOnDouble.isSelected) FinishType.Doubles else FinishType.Any
        )

    override fun getFilterDesc() = "games of ${constructGameParams().description()}"

    override fun enableChildren(enabled: Boolean) {
        spinner.isEnabled = enabled
        cbFinishOnDouble.isEnabled = enabled
    }

    override fun addActionListener(listener: ActionListener) {
        spinner.addActionListener(listener)
        cbFinishOnDouble.addActionListener(listener)
    }

    override fun removeActionListener(listener: ActionListener) {
        spinner.removeActionListener()
        cbFinishOnDouble.addActionListener(listener)
    }
}
