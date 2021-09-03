package dartzee.screen.dartzee

import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.helper.makeDartzeeRuleDto
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeRuleRenderer: AbstractTest()
{
    @Test
    fun `Should render the rule name, falling back on the description for the first column`()
    {
        val renderer = DartzeeRuleRenderer(0)
        renderer.getReplacementValue(makeDartzeeRuleDto(ruleName = "My Rule")) shouldBe "My Rule"
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