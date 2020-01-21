package dartzee.test.helper

import dartzee.`object`.DartboardSegment
import dartzee.dartzee.AbstractDartzeeRuleFactory
import dartzee.dartzee.AbstractDartzeeSegmentFactory
import dartzee.dartzee.AbstractDartzeeTemplateFactory
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeTemplateEntity

class FakeDartzeeRuleFactory(val ret: DartzeeRuleDto?): AbstractDartzeeRuleFactory()
{
    override fun newRule() = ret
    override fun amendRule(rule: DartzeeRuleDto) = ret ?: rule
}

class FakeDartzeeTemplateFactory(private val newTemplate: DartzeeTemplateEntity? = null, private val cancelCopy: Boolean = false): AbstractDartzeeTemplateFactory()
{
    override fun newTemplate() = newTemplate
    override fun copyTemplate(template: DartzeeTemplateEntity) =
            if (cancelCopy)
                null
            else {
                insertTemplateAndRule(name = template.name + " - Copy")
            }
}

class FakeDartzeeSegmentFactory(private val desiredSegments: HashSet<DartboardSegment>): AbstractDartzeeSegmentFactory()
{
    var segmentsPassedIn: HashSet<DartboardSegment> = HashSet()

    override fun selectSegments(segments: HashSet<DartboardSegment>): HashSet<DartboardSegment> {
        segmentsPassedIn = segments
        return desiredSegments
    }
}