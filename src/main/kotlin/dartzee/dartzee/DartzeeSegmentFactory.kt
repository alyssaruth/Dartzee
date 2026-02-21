package dartzee.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.screen.dartzee.DartboardSegmentSelectDialog

interface IDartzeeSegmentFactory {
    fun selectSegments(segments: Set<DartboardSegment>): Set<DartboardSegment>
}

class DartzeeSegmentFactory : IDartzeeSegmentFactory {
    override fun selectSegments(segments: Set<DartboardSegment>): Set<DartboardSegment> {
        val dlg = DartboardSegmentSelectDialog(segments)
        dlg.isVisible = true
        return dlg.getSelection()
    }
}
