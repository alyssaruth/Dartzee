package dartzee.screen

import dartzee.bean.DartLabel
import dartzee.bean.InteractiveDartboard
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.getParentWindow
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.listener.DartboardListener
import dartzee.`object`.ComputedPoint
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.SegmentStatuses
import dartzee.utils.getDartForSegment
import java.awt.Component
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.sound.sampled.Clip
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities

const val LAYER_DARTS = 2
const val LAYER_DODGY = 3
const val LAYER_SLIDER = 4

class GameplayDartboard : JLayeredPane(), MouseListener
{
    var latestClip: Clip? = null

    private val dartboard = InteractiveDartboard()
    private val dartsThrown = mutableListOf<ComputedPoint>()
    private val listeners: MutableList<DartboardListener> = mutableListOf()
    private var allowInteraction = true

    init
    {
        preferredSize = Dimension(500, 500)
        add(dartboard, Integer.valueOf(-1))

        dartboard.addMouseListener(this)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(evt: ComponentEvent) = resized()
        })

        SwingUtilities.invokeLater { resized() }
    }

    private fun resized()
    {
        dartboard.setBounds(0, 0, width, height)

        clearDartLabels()

        dartsThrown.forEach(::addDartLabel)
    }

    fun clearDarts()
    {
        dartsThrown.clear()
        clearDartLabels()
        repaint()
    }

    private fun clearDartLabels() = getAllChildComponentsForType<DartLabel>().forEach { remove(it) }

    fun addDartboardListener(listener: DartboardListener)
    {
        listeners.add(listener)
    }

    fun dartThrown(pt: ComputedPoint)
    {
        dartsThrown.add(pt)

        runOnEventThreadBlocking { addDartLabel(pt) }

        listeners.forEach { it.dartThrown(getDartForSegment(pt.segment)) }
    }

    private fun addDartLabel(computedPt: ComputedPoint)
    {
        if (dartboard.isVisible && dartboard.width > 80 && dartboard.height > 80) {
            val lbl = DartLabel()
            lbl.location = dartboard.interpretPoint(computedPt)
            add(lbl)
            setLayer(lbl, LAYER_DARTS, 5 - dartsThrown.size)
        }
    }

    fun refreshValidSegments(segmentStatuses: SegmentStatuses?)
    {
        dartboard.updateSegmentStatus(segmentStatuses)
    }

    fun stopListening()
    {
        allowInteraction = false
        dartboard.stopInteraction()
    }

    fun ensureListening()
    {
        allowInteraction = true
        dartboard.allowInteraction()
    }

    fun addOverlay(pt: Point, overlay: Component)
    {
        add(overlay)
        setLayer(overlay, LAYER_SLIDER)
        overlay.location = pt
    }

    override fun mouseReleased(arg0: MouseEvent)
    {
        if (!suppressClickForGameWindow() && allowInteraction)
        {
            dartThrown(dartboard.toComputedPoint(arg0.point))
        }
    }
    private fun suppressClickForGameWindow(): Boolean
    {
        val scrn = getParentWindow() as? AbstractDartsGameScreen ?: return false
        if (scrn.haveLostFocus)
        {
            scrn.haveLostFocus = false
            return true
        }

        return false
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}
    override fun mouseEntered(e: MouseEvent?) {}
    override fun mouseExited(e: MouseEvent?) {}
}