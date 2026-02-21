package dartzee.screen.player

import dartzee.bean.IMouseListener
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import javax.swing.JButton

abstract class PlayerSummaryButton : JButton(), ActionListener, IMouseListener {
    abstract val defaultText: String
    abstract val hoverText: String

    init {
        preferredSize = Dimension(275, 100)
        iconTextGap = 10
    }

    abstract fun buttonPressed()

    override fun actionPerformed(e: ActionEvent?) {
        buttonPressed()

        text = defaultText
    }

    override fun mouseEntered(e: MouseEvent) {
        if (isEnabled) {
            text = hoverText
        }
    }

    override fun mouseExited(e: MouseEvent) {
        text = defaultText
    }
}
