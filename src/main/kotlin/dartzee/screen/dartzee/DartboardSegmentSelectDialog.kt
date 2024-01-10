package dartzee.screen.dartzee

import dartzee.bean.IMouseListener
import dartzee.bean.PresentationDartboard
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.setMargins
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.DartboardSegment
import dartzee.`object`.WIREFRAME_COLOUR_WRAPPER
import dartzee.screen.ScreenCache
import dartzee.utils.DartsColour
import dartzee.utils.getAllNonMissSegments
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel

class DartboardSegmentSelectDialog(private val initialSegments: Set<DartboardSegment>) :
    SimpleDialog(), IMouseListener {
    private val selectedSegments = mutableSetOf<DartboardSegment>()
    private var lastDraggedSegment: DartboardSegment? = null

    private val dartboard = PresentationDartboard(WIREFRAME_COLOUR_WRAPPER, true)
    private val btnSelectAll = JButton("Select All")
    private val btnSelectNone = JButton("Select None")

    init {
        title = "Select Segments"
        setSize(550, 650)
        setLocationRelativeTo(ScreenCache.mainScreen)
        isResizable = true
        isModal = true

        val panelCenter = JPanel()
        panelCenter.layout = BorderLayout()
        panelCenter.add(dartboard, BorderLayout.CENTER)
        contentPane.add(panelCenter, BorderLayout.CENTER)

        val panelSelectionOptions = JPanel()
        panelSelectionOptions.add(btnSelectAll)
        panelSelectionOptions.add(btnSelectNone)
        panelSelectionOptions.setMargins(0, 0, 10, 0)
        panelCenter.add(panelSelectionOptions, BorderLayout.SOUTH)

        panelSelectionOptions.background = DartsColour.COLOUR_PASTEL_BLUE
        panelCenter.background = DartsColour.COLOUR_PASTEL_BLUE
        panelOkCancel.background = DartsColour.COLOUR_PASTEL_BLUE

        dartboard.addMouseListener(this)
        dartboard.addMouseMotionListener(this)

        btnSelectNone.addActionListener(this)
        btnSelectAll.addActionListener(this)

        toggleAll(initialSegments)
        lastDraggedSegment = null
    }

    fun getSelection() = selectedSegments.toSet()

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnSelectAll -> selectAll()
            btnSelectNone -> selectNone()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun selectAll() = toggleAll(getAllNonMissSegments() - selectedSegments)

    private fun selectNone() = toggleAll(selectedSegments)

    private fun toggleAll(segments: Collection<DartboardSegment>) =
        segments.toList().forEach(::toggleSegment)

    private fun toggleSegment(segment: DartboardSegment) {
        lastDraggedSegment = segment
        if (segment.isMiss()) {
            return
        }

        if (selectedSegments.contains(segment)) {
            selectedSegments.remove(segment)
            dartboard.revertOverriddenSegmentColour(segment)
        } else {
            selectedSegments.add(segment)
            val col = DEFAULT_COLOUR_WRAPPER.getColour(segment)
            dartboard.overrideSegmentColour(segment, col)
        }
    }

    override fun okPressed() {
        dispose()
    }

    override fun cancelPressed() {
        selectedSegments.clear()
        selectedSegments.addAll(initialSegments)
        dispose()
    }

    override fun mouseDragged(e: MouseEvent) {
        val segment = dartboard.getSegmentForPoint(e.point)
        if (segment == lastDraggedSegment) {
            return
        }

        toggleSegment(segment)
    }

    override fun mouseReleased(e: MouseEvent) {
        val segment = dartboard.getSegmentForPoint(e.point)
        toggleSegment(segment)
    }
}
