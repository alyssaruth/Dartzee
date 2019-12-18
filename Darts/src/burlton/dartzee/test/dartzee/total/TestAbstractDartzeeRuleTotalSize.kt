package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.AbstractDartzeeRuleTotalSize
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import java.text.ParseException

class TestAbstractDartzeeRuleTotalSize: AbstractDartsTest()
{
    @Test
    fun `Should initialise with a target of 20`()
    {
        val rule = FakeDartzeeTotalRule()
        rule.spinner.value shouldBe 20
        rule.target shouldBe 20
    }

    @Test
    fun `Should update the target when spinner value is changed`()
    {
        val rule = FakeDartzeeTotalRule()
        rule.spinner.value = 18
        rule.target shouldBe 18
    }

    @Test
    fun `Should be possible to set values between 3 and 180`()
    {
        val rule = FakeDartzeeTotalRule()
        rule.spinner.value = 3
        rule.spinner.commitEdit()
        rule.target shouldBe 3

        rule.spinner.value = 180
        rule.spinner.commitEdit()
        rule.target shouldBe 180
    }

    @Test
    fun `Should not be possible to set values lower than 3 or higher than 180`()
    {
        val rule = FakeDartzeeTotalRule()

        shouldThrow<ParseException> {
            rule.spinner.value = 181
            rule.spinner.commitEdit()
        }

        shouldThrow<ParseException> {
            rule.spinner.value = 2
            rule.spinner.commitEdit()
        }
    }

    @Test
    fun `Should initialise spinner and target when reading from XML`()
    {
        val rule = FakeDartzeeTotalRule()
        rule.target = 77

        val xml = rule.toDbString()

        val newRule = FakeDartzeeTotalRule()
        newRule.populate(xml)

        newRule.target shouldBe 77
        newRule.spinner.value shouldBe 77
    }

    private class FakeDartzeeTotalRule: AbstractDartzeeRuleTotalSize()
    {
        override fun isValidTotal(total: Int) = true
        override fun getRuleIdentifier() = "Fake"
    }
}