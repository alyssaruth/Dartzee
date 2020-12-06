package dartzee.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.screen.DartboardSegmentSelectDialog

abstract class AbstractDartzeeSegmentFactory
{
    abstract fun selectSegments(segments: Set<DartboardSegment>): Set<DartboardSegment>
}

class DartzeeSegmentFactory: AbstractDartzeeSegmentFactory()
{
    override fun selectSegments(segments: Set<DartboardSegment>): Set<DartboardSegment>
    {
        val dlg = DartboardSegmentSelectDialog(segments)
        dlg.isVisible = true
        return dlg.getSelection()
    }
}