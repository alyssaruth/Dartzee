package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.screen.dartzee.DartzeeTemplateRuleRenderer
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.makeDartzeeRuleCalculationResult
import burlton.dartzee.test.helper.makeDartzeeRuleDto
import burlton.desktopcore.test.helper.getIconImage
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import javax.swing.JLabel

class TestDartzeeTemplateRuleRenderer: AbstractDartsTest()
{
    @Test
    fun `Should render squares of the right colour based on rule difficulty`()
    {
        val renderer = DartzeeTemplateRuleRenderer()

        val ruleOne = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(20))
        val ruleTwo = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(40))

        val rendered = renderer.getLabel(listOf(ruleOne, ruleTwo))
        val img = rendered.getIconImage()

        val borderCol = Color(img.getRGB(10, 0))
        borderCol shouldBe Color.BLACK

        val col = Color(img.getRGB(10, 10))
        col shouldBe ruleOne.calculationResult!!.getForeground()

        val col2 = Color(img.getRGB(40, 10))
        col2 shouldBe ruleTwo.calculationResult!!.getForeground()
    }

    @Test
    fun `Should supply a tooltip for the number of rules`()
    {
        val renderer = DartzeeTemplateRuleRenderer()

        val lbl = renderer.getLabel(listOf(makeDartzeeRuleDto()))
        lbl.toolTipText shouldBe "1 rules"

        val lbl2 = renderer.getLabel(listOf(makeDartzeeRuleDto(), makeDartzeeRuleDto()))
        lbl2.toolTipText shouldBe "2 rules"
    }

    fun DartzeeTemplateRuleRenderer.getLabel(rules: List<DartzeeRuleDto>) = getReplacementValue(rules) as JLabel

}
