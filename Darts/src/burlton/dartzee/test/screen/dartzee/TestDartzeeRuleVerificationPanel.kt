package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.dartzee.DartzeeCalculator
import burlton.dartzee.code.screen.dartzee.DartzeeRuleVerificationPanel
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.code.utils.getAllPossibleSegments
import burlton.dartzee.test.doClick
import burlton.dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color

class TestDartzeeRuleVerificationPanel: AbstractDartsTest()
{
    override fun afterEachTest()
    {
        super.afterEachTest()

        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()
    }

    @Test
    fun `Should not re-run rule calculation if 0 darts thrown`()
    {
        val panel = DartzeeRuleVerificationPanel()

        val d20 = panel.dartboard.getSegment(20, SEGMENT_TYPE_DOUBLE)!!

        val dto = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(listOf(d20)))

        panel.updateRule(dto)

        panel.dartboard.validSegments.shouldContainExactly(d20)
    }

    @Test
    fun `Should update the dart history and clear it on reset`()
    {
        val panel = DartzeeRuleVerificationPanel()
        panel.updateRule(makeDartzeeRuleDto())

        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.dartThrown(makeDart(2, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.tfResult.text shouldBe "1 → 2 → ?, Total: 3"

        panel.btnReset.doClick()
        panel.tfResult.text shouldBe "? → ? → ?, Total: 0"
    }

    @Test
    fun `Should calculate the total based on the rule`()
    {
        val panel = DartzeeRuleVerificationPanel()
        panel.updateRule(makeDartzeeRuleDto(dart1Rule = makeScoreRule(2)))

        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.dartThrown(makeDart(2, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.tfResult.text shouldBe "1 → 2 → ?, Total: 2"
    }

    @Test
    fun `Should update the dartboard with valid segments as darts are thrown`()
    {
        val panel = DartzeeRuleVerificationPanel()
        val dartboard = panel.dartboard

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        dartboard.validSegments.shouldContainExactly(getFakeValidSegment(0))
        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))

        dartboard.validSegments.shouldContainExactly(getFakeValidSegment(1))
        panel.dartThrown(makeDart(2, 1, SEGMENT_TYPE_INNER_SINGLE))

        dartboard.validSegments.shouldContainExactly(getFakeValidSegment(2))
        panel.dartThrown(makeDart(20, 2, SEGMENT_TYPE_DOUBLE))

        //Shouldn't update on the last dart thrown
        dartboard.validSegments.shouldContainExactly(getFakeValidSegment(2))
    }

    @Test
    fun `Should stay blue while the rule is still possible, and go green when 3 valid darts are thrown`()
    {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)
        panel.shouldBeBlue()

        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.shouldBeBlue()

        panel.dartThrown(makeDart(2, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.shouldBeBlue()

        panel.dartThrown(makeDart(3, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.shouldBeGreen()
    }

    @Test
    fun `Should go red and invalidate the total as soon as an invalid dart is thrown`()
    {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        panel.dartThrown(makeDart(20, 0, SEGMENT_TYPE_MISS))
        panel.shouldBeRed()

        panel.tfResult.text shouldBe "0 → ? → ?, Total: N/A"
    }

    @Test
    fun `Should update dartboard colour when the rule changes`()
    {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        panel.dartThrown(makeDart(20, 0, SEGMENT_TYPE_MISS))
        panel.shouldBeRed()

        val updatedRule = makeDartzeeRuleDto(allowMisses = true)
        panel.updateRule(updatedRule)

        panel.shouldBeBlue()
    }

    @Test
    fun `Should reset color when darts are cleared`()
    {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        panel.dartThrown(makeDart(20, 0, SEGMENT_TYPE_MISS))
        panel.shouldBeRed()

        panel.btnReset.doClick()
        panel.shouldBeBlue()
    }

    @Test
    fun `Should update combinations text when darts are thrown`()
    {
        val panel  = DartzeeRuleVerificationPanel()
        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)
        panel.lblCombinations.text shouldBe "1 combinations (success%: 10.0%)"

        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.lblCombinations.text shouldBe "1 combinations (success%: 20.0%)"
    }

    @Test
    fun `Should stop listening for clicks once three darts have been thrown, and listen again when reset is pressed`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val panel  = DartzeeRuleVerificationPanel()
        val rule = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments()))
        panel.updateRule(rule)

        panel.dartboard.doClick(20, 30)
        panel.dartboard.doClick(30, 20)
        panel.dartboard.doClick(40, 10)

        panel.dartsThrown.size shouldBe 3

        panel.dartboard.doClick(20, 30)
        panel.dartsThrown.size shouldBe 3

        panel.btnReset.doClick()
        panel.dartsThrown.shouldBeEmpty()

        panel.dartboard.doClick(20, y = 30)
        panel.dartsThrown.size shouldBe 1
    }

    private fun DartzeeRuleVerificationPanel.shouldBeBlue()
    {
        tfResult.foreground shouldBe Color.WHITE
        background shouldBe DartsColour.COLOUR_PASTEL_BLUE
    }
    private fun DartzeeRuleVerificationPanel.shouldBeRed()
    {
        tfResult.foreground shouldBe Color.RED
        background shouldBe DartsColour.getDarkenedColour(Color.RED)
    }
    private fun DartzeeRuleVerificationPanel.shouldBeGreen()
    {
        tfResult.foreground shouldBe Color.GREEN
        background shouldBe DartsColour.getDarkenedColour(Color.GREEN)
    }
}