package dartzee.screen

import dartzee.listener.DartboardListener
import dartzee.`object`.ColourWrapper
import dartzee.`object`.ComputedPoint
import dartzee.screen.game.dartzee.SegmentStatuses
import java.awt.Component
import java.awt.Point
import javax.swing.JLayeredPane

interface ITempDartboardBase
{
    var renderScoreLabels: Boolean
    var renderDarts: Boolean

    fun clearDarts()
    fun dartThrown(pt: ComputedPoint)
    fun addDartboardListener(listener: DartboardListener)
    fun ensureListening()
    fun stopListening()
    fun paintDartboard(colourWrapper: ColourWrapper? = null)
    fun refreshValidSegments(segmentStatuses: SegmentStatuses?)
}

abstract class TempDartboardBase : JLayeredPane(), ITempDartboardBase
{
    override var renderDarts = false
    override var renderScoreLabels = false

    override fun paintDartboard(colourWrapper: ColourWrapper?) {}

    fun addOverlay(pt: Point, overlay: Component)
    {
        add(overlay)
        setLayer(overlay, LAYER_SLIDER)
        overlay.location = pt
    }
}