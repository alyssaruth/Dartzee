package burlton.dartzee.code.screen

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.utils.DartsColour
import burlton.desktopcore.code.screen.SimpleDialog
import java.awt.BorderLayout
import javax.swing.JPanel

class DartboardSegmentSelectDialog(private val segments: HashSet<DartboardSegmentKt>): SimpleDialog()
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

        dartboard.initState(segments)
    }

    fun getSelection(): HashSet<DartboardSegmentKt>
    {
        return dartboard.selectedSegments
    }

    override fun okPressed()
    {
        dispose()
    }

    override fun cancelPressed()
    {
        dartboard.selectedSegments = segments
        dispose()
    }
}