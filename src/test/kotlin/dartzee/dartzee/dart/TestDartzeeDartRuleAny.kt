package dartzee.dartzee.dart

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.utils.getAllSegmentsForDartzee
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleAny: AbstractDartzeeRuleTest<DartzeeDartRuleAny>()
{
    override fun factory() = DartzeeDartRuleAny()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleAny()

        getAllSegmentsForDartzee().forEach {
            rule.isValidSegment(it) shouldBe true
        }
    }
}