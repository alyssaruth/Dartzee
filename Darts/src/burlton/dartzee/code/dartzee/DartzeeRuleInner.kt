package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import org.w3c.dom.Element

class DartzeeRuleInner : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.type == SEGMENT_TYPE_INNER_SINGLE
            || segment.type == SEGMENT_TYPE_TREBLE
            || segment.score == 25
    }

    override fun getRuleIdentifier() = "Inner"

    override fun writeXmlAttributes(rootElement: Element) {}
    override fun populate(rootElement: Element) {}
}