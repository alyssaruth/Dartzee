package dartzee.test.screen.dartzee

import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.screen.dartzee.DartzeeRuleRenderer
import dartzee.test.helper.AbstractTest
import dartzee.test.helper.makeDartzeeRuleCalculationResult
import dartzee.test.helper.makeDartzeeRuleDto
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleRenderer: AbstractTest()
{
    @Test
    fun `Should render the rule description for the first column`()
    {
        val renderer = DartzeeRuleRenderer(0)
        renderer.getReplacementValue(makeDartzeeRuleDto()) shouldBe "Anything"
        renderer.getReplacementValue(makeDartzeeRuleDto(DartzeeDartRuleInner())) shouldBe "Score Inners"
    }

    @Test
    fun `Should render the difficulty for the second column`()
    {
        val renderer = DartzeeRuleRenderer(1)
        val veryEasyRule = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(50))
        val veryHardRule = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(2))

        renderer.getReplacementValue(veryEasyRule) shouldBe "Very Easy"
        renderer.getReplacementValue(veryHardRule) shouldBe "Very Hard"
    }

    @Test
    fun `Should set cell colours based on rule difficulty`()
    {
        val renderer = DartzeeRuleRenderer(1)
        val veryEasyRule = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(50))
        val veryHardRule = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(2))

        renderer.setCellColours(veryEasyRule, true)
        renderer.foreground shouldBe veryEasyRule.calculationResult!!.getForeground()
        renderer.background shouldBe veryEasyRule.calculationResult!!.getBackground()

        renderer.setCellColours(veryHardRule, true)
        renderer.foreground shouldBe veryHardRule.calculationResult!!.getForeground()
        renderer.background shouldBe veryHardRule.calculationResult!!.getBackground()
    }

    @Test
    fun `Should use size 20 font`()
    {
        val renderer = DartzeeRuleRenderer(0)

        renderer.setFontsAndAlignment()
        renderer.font.size shouldBe 20
    }
}