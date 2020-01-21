package dartzee.test.dartzee.dart

import dartzee.dartzee.dart.DartzeeDartRuleAny
import dartzee.utils.getAllPossibleSegments
import dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

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