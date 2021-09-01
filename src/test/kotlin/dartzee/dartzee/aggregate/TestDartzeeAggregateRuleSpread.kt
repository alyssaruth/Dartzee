package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.text.ParseException

class TestDartzeeAggregateRuleSpread: AbstractDartzeeRuleTest<DartzeeAggregateRuleSpread>()
{
    override fun factory() = DartzeeAggregateRuleSpread()

    override val emptyIsValid = false

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
    fun `Should not be possible to set values lower than 3 or higher than 180`()
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
}