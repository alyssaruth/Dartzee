package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.TYPE_TREBLE
import org.w3c.dom.Element

class DartzeeRuleInner : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.type == TYPE_INNER_SINGLE
            || segment.type == TYPE_TREBLE
            || segment.score == 25
    }

    override fun getRuleIdentifier() = "Inner"

    override fun writeXmlAttributes(rootElement: Element) {}
    override fun populate(rootElement: Element) {}
}