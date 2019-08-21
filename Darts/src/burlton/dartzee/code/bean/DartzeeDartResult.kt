package burlton.dartzee.code.bean

import burlton.dartzee.code.`object`.Dart
import javax.swing.JLabel
import javax.swing.JPanel

class DartzeeDartResult: JPanel()
{
    private val label = JLabel()

    init
    {
        add(label)
    }

    fun reset()
    {
        label.text = ""
        repaint()
    }

    fun update(dart: Dart)
    {
        label.text = dart.getRendered()
        repaint()
    }

}