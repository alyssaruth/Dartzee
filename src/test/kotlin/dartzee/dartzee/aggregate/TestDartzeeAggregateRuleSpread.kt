package dartzee.dartzee.aggregate

import dartzee.bullseye
import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.dartzee.parseAggregateRule
import dartzee.helper.double
import dartzee.helper.miss
import dartzee.helper.outerSingle
import dartzee.helper.treble
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.text.ParseException

class TestDartzeeAggregateRuleSpread: AbstractDartzeeRuleTest<DartzeeAggregateRuleSpread>()
{
    override fun factory() = DartzeeAggregateRuleSpread()

    @Test
    fun `Should initialise with a spread of 1`()
    {
        val rule = DartzeeAggregateRuleSpread()
        rule.spinner.value shouldBe 1
    }

    @Test
    fun `Should be possible to set values between 1 and 5`()
    {
        val rule = DartzeeAggregateRuleSpread()
        rule.spinner.value = 1
        rule.spinner.commitEdit()
        rule.spinner.value shouldBe 1

        rule.spinner.value = 5
        rule.spinner.commitEdit()
        rule.spinner.value shouldBe 5
    }

    @Test
    fun `Should not be possible to set values lower than 1 or higher than 5`()
    {
        val rule = DartzeeAggregateRuleSpread()

        shouldThrow<ParseException> {
            rule.spinner.value = 0
            rule.spinner.commitEdit()
        }

        shouldThrow<ParseException> {
            rule.spinner.value = 6
            rule.spinner.commitEdit()
        }
    }

    @Test
    fun `Should have the correct description`()
    {
        val rule = DartzeeAggregateRuleSpread()
        rule.getDescription() shouldBe "Darts spaced by at least 1"

        rule.spinner.value = 4
        rule.getDescription() shouldBe "Darts spaced by at least 4"
    }

    @Test
    fun `Should write and read XML correctly`()
    {
        val rule = factory()
        rule.spinner.value = 3

        val xml = rule.toDbString()
        val deserialisedRule = parseAggregateRule(xml)

        deserialisedRule.shouldBeInstanceOf<DartzeeAggregateRuleSpread> {
            it.spinner.value shouldBe 3
        }
    }

    @Test
    fun `Should not be valid if a dart has missed`()
    {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(10), miss(14))) shouldBe false
    }

    @Test
    fun `Should not be valid if bullseye was hit`()
    {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(10), bullseye)) shouldBe false
    }

    @Test
    fun `Should not be valid if a number is repeated`()
    {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(20), outerSingle(3))) shouldBe false
    }

    val oneApart = listOf(outerSingle(20), outerSingle(18), outerSingle(12))
    val twoApart = listOf(outerSingle(20), double(4), treble(10))
    val threeApart = listOf(outerSingle(19), treble(11), outerSingle(5))
    val fourApart = listOf(outerSingle(10), outerSingle(1), outerSingle(14))
    val fiveApart = listOf(outerSingle(7), double(12), outerSingle(13))

    @Test
    fun `Should not be valid if two adjacent numbers are hit`()
    {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(1), outerSingle(3))) shouldBe false
        factory().isValidRound(listOf(outerSingle(1), outerSingle(20), outerSingle(3))) shouldBe false
        factory().isValidRound(listOf(outerSingle(3), outerSingle(1), outerSingle(20))) shouldBe false
    }

    @Test
    fun `Should validate correctly with spread of 1`()
    {
        factory().isValidRound(listOf(outerSingle(19), outerSingle(17), outerSingle(15))) shouldBe true
        factory().isValidRound(listOf(outerSingle(20), outerSingle(11), outerSingle(10))) shouldBe true
        factory().isValidRound(oneApart) shouldBe true
        factory().isValidRound(twoApart) shouldBe true
        factory().isValidRound(threeApart) shouldBe true
        factory().isValidRound(fourApart) shouldBe true
        factory().isValidRound(fiveApart) shouldBe true
    }

    @Test
    fun `Should validate correctly with spread of 2`()
    {
        val rule = factory().also { it.spinner.value = 2 }

        rule.isValidRound(oneApart) shouldBe false
        rule.isValidRound(twoApart) shouldBe true
        rule.isValidRound(threeApart) shouldBe true
        rule.isValidRound(fourApart) shouldBe true
        rule.isValidRound(fiveApart) shouldBe true
    }

    @Test
    fun `Should validate correctly with spread of 3`()
    {
        val rule = factory().also { it.spinner.value = 3 }

        rule.isValidRound(oneApart) shouldBe false
        rule.isValidRound(twoApart) shouldBe false
        rule.isValidRound(threeApart) shouldBe true
        rule.isValidRound(fourApart) shouldBe true
        rule.isValidRound(fiveApart) shouldBe true
    }

    @Test
    fun `Should validate correctly with spread of 4`()
    {
        val rule = factory().also { it.spinner.value = 4 }

        rule.isValidRound(oneApart) shouldBe false
        rule.isValidRound(twoApart) shouldBe false
        rule.isValidRound(threeApart) shouldBe false
        rule.isValidRound(fourApart) shouldBe true
        rule.isValidRound(fiveApart) shouldBe true
    }

    @Test
    fun `Should validate correctly with spread of 5`()
    {
        val rule = factory().also { it.spinner.value = 5 }

        rule.isValidRound(oneApart) shouldBe false
        rule.isValidRound(twoApart) shouldBe false
        rule.isValidRound(threeApart) shouldBe false
        rule.isValidRound(fourApart) shouldBe false
        rule.isValidRound(fiveApart) shouldBe true
    }
}