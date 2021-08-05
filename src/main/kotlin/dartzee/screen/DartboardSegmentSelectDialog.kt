package dartzee.screen

import dartzee.`object`.DartboardSegment
import dartzee.core.screen.SimpleDialog
import dartzee.utils.DartsColour
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JPanel

class DartboardSegmentSelectDialog(private val segments: Set<DartboardSegment>): SimpleDialog()
{
    private val dartboard = DartboardSegmentSelector()
    private val btnSelectAll = JButton("Select All")
    private val btnSelectNone = JButton("Select None")

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

        val panelSelectionOptions = JPanel()
        panelSelectionOptions.add(btnSelectAll)
        panelSelectionOptions.add(btnSelectNone)
        panelCenter.add(panelSelectionOptions, BorderLayout.SOUTH)

        dartboard.paintDartboard()

        panelSelectionOptions.background = DartsColour.COLOUR_PASTEL_BLUE
        panelCenter.background = DartsColour.COLOUR_PASTEL_BLUE
        panelOkCancel.background = DartsColour.COLOUR_PASTEL_BLUE

        dartboard.initState(segments)

        btnSelectNone.addActionListener(this)
        btnSelectAll.addActionListener(this)
    }

    fun getSelection(): Set<DartboardSegment>
    {
        return dartboard.selectedSegments
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnSelectAll -> dartboard.selectAll()
            btnSelectNone -> dartboard.selectNone()
            else -> super.actionPerformed(arg0)
        }
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