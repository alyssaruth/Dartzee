package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.screen.DartboardSegmentSelectDialog

abstract class AbstractDartzeeSegmentFactory
{
    abstract fun selectSegments(segments: HashSet<DartboardSegment>): HashSet<DartboardSegment>
}

class DartzeeSegmentFactory: AbstractDartzeeSegmentFactory()
{
    override fun selectSegments(segments: HashSet<DartboardSegment>): HashSet<DartboardSegment>
    {
        val dlg = DartboardSegmentSelectDialog(segments)
        dlg.isVisible = true
        return dlg.getSelection()
    }
}