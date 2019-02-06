package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt
import org.w3c.dom.Element

class DartzeeDartRuleEven : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return (segment.getTotal() % 2 == 0)
    }

    override fun getRuleIdentifier() = "Even"
    override fun writeXmlAttributes(rootElement: Element) {}
    override fun populate(rootElement: Element) {}
}