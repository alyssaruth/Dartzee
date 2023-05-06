package dartzee.screen

import dartzee.bean.DartLabel
import dartzee.bean.InteractiveDartboard
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.getParentWindow
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.`object`.ComputedPoint
import dartzee.screen.game.DartsGameScreen
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JLayeredPane

class GameplayDartboard : JLayeredPane(), MouseListener
{
    private val dartboard = InteractiveDartboard()
    private val dartsThrown = mutableListOf<ComputedPoint>()

    init
    {
        isOpaque = false
        add(dartboard, Integer.valueOf(-1))

        dartboard.addMouseListener(this)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(evt: ComponentEvent) = resized()
        })
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

    private fun dartThrown(pt: ComputedPoint)
    {
        dartsThrown.add(pt)

        runOnEventThreadBlocking { addDartLabel(pt) }
    }

    private fun addDartLabel(computedPt: ComputedPoint)
    {
        val lbl = DartLabel()
        lbl.location = dartboard.interpretPoint(computedPt)
        add(lbl)
        setLayer(lbl, LAYER_DARTS, 5-dartsThrown.size)
    }

    override fun mouseReleased(arg0: MouseEvent)
    {
        if (!suppressClickForGameWindow())
        {
            dartThrown(dartboard.toComputedPoint(arg0.point))
        }
    }
    private fun suppressClickForGameWindow(): Boolean
    {
        val scrn = getParentWindow() as? DartsGameScreen ?: return false
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