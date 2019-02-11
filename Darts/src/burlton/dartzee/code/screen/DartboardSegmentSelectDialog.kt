package burlton.dartzee.code.screen

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.utils.DartsColour
import burlton.desktopcore.code.screen.SimpleDialog
import java.awt.BorderLayout
import javax.swing.JPanel

class DartboardSegmentSelectDialog: SimpleDialog(), DartboardListener
{
    private val dartboard = DartboardSegmentSelector()

    init
    {
        title = "Select Segments"
        setSize(500, 600)
        setLocationRelativeTo(ScreenCache.getMainScreen())
        isResizable = false
        isModal = true

        val panelCenter = JPanel()
        panelCenter.layout = BorderLayout()
        panelCenter.add(dartboard, BorderLayout.CENTER)
        contentPane.add(panelCenter, BorderLayout.CENTER)


        dartboard.paintDartboard()

        panelCenter.background = DartsColour.COLOUR_PASTEL_BLUE
        panelOkCancel.background = DartsColour.COLOUR_PASTEL_BLUE
    }

    override fun okPressed()
    {
        dispose()
    }

    override fun dartThrown(dart: Dart)
    {

    }
}