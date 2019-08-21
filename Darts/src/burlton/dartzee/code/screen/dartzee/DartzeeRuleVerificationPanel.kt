package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel

class DartzeeRuleVerificationPanel: JPanel(), DartboardListener
{
    private val dartboard = Dartboard(300, 300)
    private val dartsThrown = mutableListOf<Dart>()

    init
    {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(300, 300)

        dartboard.paintDartboard()
        add(dartboard, BorderLayout.CENTER)
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)
    }
}