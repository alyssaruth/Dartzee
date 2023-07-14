package dartzee.helper

import dartzee.dartzee.AbstractDartzeeRuleFactory
import dartzee.dartzee.AbstractDartzeeSegmentFactory
import dartzee.dartzee.DartzeeRuleDto
import dartzee.`object`.DartboardSegment

class FakeDartzeeRuleFactory(val ret: DartzeeRuleDto?): AbstractDartzeeRuleFactory()
{
    override fun newRule() = ret
    override fun amendRule(rule: DartzeeRuleDto) = ret ?: rule
}

class FakeDartzeeSegmentFactory(private val desiredSegments: Set<DartboardSegment>): AbstractDartzeeSegmentFactory()
{
    var segmentsPassedIn: Set<DartboardSegment> = emptySet()

    override fun selectSegments(segments: Set<DartboardSegment>): Set<DartboardSegment> {
        segmentsPassedIn = segments
        return desiredSegments
    }
}