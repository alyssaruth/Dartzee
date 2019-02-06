package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt
import org.w3c.dom.Element

class DartzeeDartRuleOdd : AbstractDartzeeDartRule()
{
    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return (segment.getTotal() % 2 != 0)
    }

    override fun getRuleIdentifier() = "Odd"
    override fun writeXmlAttributes(rootElement: Element) {}
    override fun populate(rootElement: Element) {}
}