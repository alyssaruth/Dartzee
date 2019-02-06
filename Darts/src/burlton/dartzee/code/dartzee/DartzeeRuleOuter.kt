package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import org.w3c.dom.Element

class DartzeeRuleOuter : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.type == SEGMENT_TYPE_OUTER_SINGLE || segment.type == SEGMENT_TYPE_DOUBLE
    }

    override fun getRuleIdentifier() = "Outer"
    override fun writeXmlAttributes(rootElement: Element) {}
    override fun populate(rootElement: Element) {}
}