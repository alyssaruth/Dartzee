package burlton.dartzee.test.dartzee

import burlton.dartzee.code.bean.SpinnerSingleSelector
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleCustom
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleScore
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.test.*
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDartzeeRules: AbstractDartsTest()
{
    @Test
    fun `can't create empty custom rule`()
    {
        val rule = DartzeeDartRuleCustom()

        assertFalse(rule.validate().isEmpty())
    }

    @Test
    fun `a custom rule with at least one segment is valid`()
    {
        val rule = DartzeeDartRuleCustom()
        rule.segments = hashSetOf(doubleTwenty)

        assertTrue(rule.validate().isEmpty())
    }

    @Test
    fun `segment validation - score`()
    {
        val rule = DartzeeDartRuleScore()
        rule.score = 20

        assertTrue(rule.isValidSegment(singleTwenty))
        assertTrue(rule.isValidSegment(doubleTwenty))
        assertTrue(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(miss))
    }

    @Test
    fun `no rules should have overlapping identifiers`()
    {
        val rules = getAllDartRules()

        val nameCount = rules.map{ it.getRuleIdentifier() }.distinct().count()

        assertEquals(nameCount, rules.size)
    }

    @Test
    fun `sensible toString implementation`()
    {
        val rule = DartzeeDartRuleOuter()

        rule.getRuleIdentifier() shouldBe "$rule"
    }

    @Test
    fun `invalid XML should return null rule`()
    {
        val rule = parseDartRule("BAD")
        rule shouldBe null
    }

    @Test
    fun `invalid identifier in XML should return null rule`()
    {
        val rule = parseDartRule("<Broken/>")
        rule shouldBe null
    }

    @Test
    fun `write score XML`()
    {
        val rule = DartzeeDartRuleScore()
        rule.score = 20

        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml) as DartzeeDartRuleScore

        parsedRule.score shouldBe 20
    }

    @Test
    fun `write custom XML`()
    {
        val rule = DartzeeDartRuleCustom()

        rule.segments = hashSetOf(doubleTwenty, outerBull, trebleNineteen)

        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml)

        assertTrue(parsedRule is DartzeeDartRuleCustom)

        parsedRule.segments shouldHaveSize(3)

        parsedRule.isValidSegment(doubleTwenty) shouldBe true
        parsedRule.isValidSegment(singleTwenty) shouldBe false
    }

    @Test
    fun `write simple XML`()
    {
        val rule = DartzeeDartRuleEven()
        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml)!!

        parsedRule.shouldBeInstanceOf<DartzeeDartRuleEven>()
    }

    @Test
    fun `Score config panel updates rule correctly`()
    {
        val rule = DartzeeDartRuleScore()

        val panel = rule.configPanel

        val spinner = panel.components.filterIsInstance(SpinnerSingleSelector::class.java).first()

        assertNotNull(spinner)

        assertEquals(spinner.value, rule.score)
        assertTrue(rule.score > -1)

        for (i in 1..25)
        {
            spinner.value = i
            rule.stateChanged(null)

            assertEquals(spinner.value, rule.score)
            assertFalse(rule.score in 21..24)
        }
    }
}
