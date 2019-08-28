package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleCustom
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import kotlin.test.assertTrue

class TestDartzeeRuleCustom: AbstractDartzeeRuleTest<DartzeeDartRuleCustom>()
{
    override val emptyIsValid = false

    override fun factory() = DartzeeDartRuleCustom()

    @Test
    fun `a custom rule with at least one segment is valid`()
    {
        val rule = DartzeeDartRuleCustom()
        rule.segments = hashSetOf(doubleTwenty)

        rule.validate().shouldBeEmpty()
    }

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleCustom()
        rule.segments = hashSetOf(doubleTwenty, trebleNineteen)

        rule.isValidSegment(doubleTwenty) shouldBe true
        rule.isValidSegment(trebleNineteen) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe false
    }

    @Test
    fun `Read and write XML`()
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
}
