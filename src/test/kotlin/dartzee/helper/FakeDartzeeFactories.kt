package dartzee.helper

import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.IDartzeeRuleFactory
import dartzee.dartzee.IDartzeeSegmentFactory
import dartzee.`object`.DartboardSegment

class FakeDartzeeRuleFactory(private val ret: DartzeeRuleDto?): IDartzeeRuleFactory
{
    override fun newRule() = ret
    override fun amendRule(rule: DartzeeRuleDto) = ret ?: rule
}

class FakeDartzeeSegmentFactory(private val desiredSegments: Set<DartboardSegment>): IDartzeeSegmentFactory
{
    var segmentsPassedIn: Set<DartboardSegment> = emptySet()

    override fun selectSegments(segments: Set<DartboardSegment>): Set<DartboardSegment> {
        segmentsPassedIn = segments
        return desiredSegments
    }
}