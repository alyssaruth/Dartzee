package dartzee.core.bean

import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.JLabel

class LinkLabel(text: String, private val linkClicked: () -> Unit): JLabel("<html><u>$text</u></html>"), IHyperlinkListener
{
    init
    {
        foreground = Color.BLUE

        val adaptor = HyperlinkAdaptor(this)
        addMouseListener(adaptor)
        addMouseMotionListener(adaptor)
    }

    override fun linkClicked(arg0: MouseEvent) = linkClicked()
    override fun isOverHyperlink(arg0: MouseEvent) = true
}