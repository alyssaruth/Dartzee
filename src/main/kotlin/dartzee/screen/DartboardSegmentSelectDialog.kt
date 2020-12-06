package dartzee.screen

import dartzee.`object`.DartboardSegment
import dartzee.core.screen.SimpleDialog
import dartzee.utils.DartsColour
import java.awt.BorderLayout
import javax.swing.JPanel

class DartboardSegmentSelectDialog(private val segments: Set<DartboardSegment>): SimpleDialog()
{
    private val dartboard = DartboardSegmentSelector()

    init
    {
        title = "Select Segments"
        setSize(500, 600)
        setLocationRelativeTo(ScreenCache.mainScreen)
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

    fun getSelection(): Set<DartboardSegment>
    {
        return dartboard.selectedSegments
    }

    override fun okPressed()
    {
        dispose()
    }

    override fun cancelPressed()
    {
        dartboard.selectedSegments = segments.toMutableSet()
        dispose()
    }
}