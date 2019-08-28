package burlton.dartzee.test.dartzee

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.code.dartzee.getAllTotalRules
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRules: AbstractDartsTest()
{
    @Test
    fun `no dart rules should have overlapping identifiers`()
    {
        val rules = getAllDartRules()

        val nameCount = rules.map{ it.getRuleIdentifier() }.distinct().count()
        nameCount shouldBe rules.size
    }

    @Test
    fun `no total rules should have overlapping identifiers`()
    {
        val rules = getAllTotalRules()

        val nameCount = rules.map{ it.getRuleIdentifier() }.distinct().count()
        nameCount shouldBe rules.size
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
    fun `write simple XML`()
    {
        val rule = DartzeeDartRuleEven()
        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml)!!

        parsedRule.shouldBeInstanceOf<DartzeeDartRuleEven>()
    }
}
