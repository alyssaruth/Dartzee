package dartzee.dartzee.dart

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.utils.getAllPossibleSegments
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleAny: AbstractDartzeeRuleTest<DartzeeDartRuleAny>()
{
    override fun factory() = DartzeeDartRuleAny()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleAny()

        getAllPossibleSegments().forEach {
            rule.isValidSegment(it) shouldBe true
        }
    }
}