package dartzee.screen.player

import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton

abstract class PlayerSummaryButton: JButton(), ActionListener, MouseListener
{
    abstract val defaultText: String
    abstract val hoverText: String

    init
    {
        preferredSize = Dimension(275, 100)
        iconTextGap = 10
    }

    abstract fun buttonPressed()

    override fun actionPerformed(e: ActionEvent?)
    {
        buttonPressed()

        text = defaultText
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?)
    {
        if (isEnabled)
        {
            text = hoverText
        }
    }

    override fun mouseExited(e: MouseEvent?)
    {
        text = defaultText
    }
}