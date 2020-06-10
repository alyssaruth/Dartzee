package dartzee.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.core.screen.SimpleDialog
import dartzee.screen.DartboardSegmentSelector
import dartzee.screen.ScreenCache
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.DartsColour
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class DartzeeAimCalculatorTest: SimpleDialog()
{
    private val segments: HashSet<DartboardSegment> = HashSet()

    private val lblResult = JLabel()
    private val dartboard = DartboardSegmentSelector()
    private val calculator = DartzeeAimCalculator()

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
        panelCenter.add(lblResult, BorderLayout.NORTH)


        dartboard.paintDartboard()

        panelCenter.background = DartsColour.COLOUR_PASTEL_BLUE
        panelOkCancel.background = DartsColour.COLOUR_PASTEL_BLUE

        dartboard.initState(segments)
    }

    override fun okPressed()
    {
        val segmentStatus = SegmentStatus(dartboard.selectedSegments, dartboard.selectedSegments)
        val segment = calculator.getSegmentToAimFor(segmentStatus)

        lblResult.text = "$segment"
        lblResult.repaint()
    }

    override fun cancelPressed()
    {
        dartboard.selectedSegments = segments
        dispose()
    }
}