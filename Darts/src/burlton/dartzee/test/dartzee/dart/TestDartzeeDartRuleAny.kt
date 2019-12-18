package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleAny
import burlton.dartzee.code.utils.getAllPossibleSegments
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
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