package dartzee.bean

import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JPanel

abstract class GameParamFilterPanel : JPanel() {
    init {
        layout = BorderLayout(0, 0)
    }

    abstract fun setGameParams(gameParams: String)

    abstract fun getGameParams(): String

    abstract fun getFilterDesc(): String

    abstract fun enableChildren(enabled: Boolean)

    abstract fun addActionListener(listener: ActionListener)

    abstract fun removeActionListener(listener: ActionListener)

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        enableChildren(enabled)
    }
}
