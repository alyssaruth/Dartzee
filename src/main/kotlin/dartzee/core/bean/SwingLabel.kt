package dartzee.core.bean

import java.awt.Graphics
import javax.swing.JLabel

class SwingLabel(text: String, val testId: String = ""): JLabel(text)
{
    override fun paint(g: Graphics?)
    {
        enableAntiAliasing(g)
        super.paint(g)
    }
}