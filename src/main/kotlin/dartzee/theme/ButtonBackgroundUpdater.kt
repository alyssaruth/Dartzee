package dartzee.theme

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton

class ButtonBackgroundUpdater(private val button: JButton) : MouseAdapter() {
    private val bgColour = button.background

    override fun mousePressed(e: MouseEvent?) {
        button.background = bgColour.darker()
    }

    override fun mouseReleased(e: MouseEvent?) {
        button.background = bgColour
    }

    override fun mouseEntered(e: MouseEvent?) {
        button.background = bgColour.brighter()
    }

    override fun mouseExited(e: MouseEvent?) {
        button.background = bgColour
    }
}
