package dartzee.screen.dartzee

import com.github.alyssaburlton.swingtest.flushEdt
import dartzee.dartzee.DartzeeCalculator
import dartzee.helper.AbstractTest
import dartzee.helper.getFakeValidSegment
import dartzee.helper.makeDart
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.makeScoreRule
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.segmentStatuses
import dartzee.throwDartByClick
import dartzee.utils.DartsColour
import dartzee.utils.InjectedThings
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import org.junit.jupiter.api.Test

class TestDartzeeRuleVerificationPanel : AbstractTest() {
    @Test
    fun `Should not re-run rule calculation if 0 darts thrown`() {
        val panel = DartzeeRuleVerificationPanel()

        val dto =
            makeDartzeeRuleDto(
                calculationResult =
                    makeDartzeeRuleCalculationResult(
                        listOf(DartboardSegment(SegmentType.DOUBLE, 20))
                    )
            )

        panel.updateRule(dto)

        panel.dartboard
            .segmentStatuses()!!
            .scoringSegments
            .shouldContainExactly(DartboardSegment(SegmentType.DOUBLE, 20))
    }

    @Test
    fun `Should update the dart history and clear it on reset`() {
        val panel = DartzeeRuleVerificationPanel()
        panel.updateRule(makeDartzeeRuleDto())

        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))
        panel.dartThrown(makeDart(2, 1, SegmentType.INNER_SINGLE))
        panel.tfResult.text shouldBe "1 → 2 → ?, Total: 3"

        panel.btnReset.doClick()
        panel.tfResult.text shouldBe "? → ? → ?, Total: 0"
    }

    @Test
    fun `Should calculate the total based on the rule`() {
        val panel = DartzeeRuleVerificationPanel()
        panel.updateRule(makeDartzeeRuleDto(dart1Rule = makeScoreRule(2)))

        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))
        panel.dartThrown(makeDart(2, 1, SegmentType.INNER_SINGLE))
        panel.tfResult.text shouldBe "1 → 2 → ?, Total: 2"
    }

    @Test
    fun `Should update the dartboard with valid segments as darts are thrown`() {
        val panel = DartzeeRuleVerificationPanel()
        val dartboard = panel.dartboard

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        dartboard.segmentStatuses()!!.validSegments.shouldContainExactly(getFakeValidSegment(0))
        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))

        dartboard.segmentStatuses()!!.validSegments.shouldContainExactly(getFakeValidSegment(1))
        panel.dartThrown(makeDart(2, 1, SegmentType.INNER_SINGLE))

        dartboard.segmentStatuses()!!.validSegments.shouldContainExactly(getFakeValidSegment(2))
        panel.dartThrown(makeDart(20, 2, SegmentType.DOUBLE))

        // Shouldn't update on the last dart thrown
        dartboard.segmentStatuses()!!.validSegments.shouldContainExactly(getFakeValidSegment(2))
    }

    @Test
    fun `Should stay blue while the rule is still possible, and go green when 3 valid darts are thrown`() {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)
        panel.shouldBeBlue()

        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))
        panel.shouldBeBlue()

        panel.dartThrown(makeDart(2, 1, SegmentType.INNER_SINGLE))
        panel.shouldBeBlue()

        panel.dartThrown(makeDart(3, 1, SegmentType.INNER_SINGLE))
        panel.shouldBeGreen()
    }

    @Test
    fun `Should go red and invalidate the total as soon as an invalid dart is thrown`() {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        panel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        panel.shouldBeRed()

        panel.tfResult.text shouldBe "0 → ? → ?, Total: N/A"
    }

    @Test
    fun `Should update dartboard colour when the rule changes`() {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        panel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        panel.shouldBeRed()

        val updatedRule = makeDartzeeRuleDto(allowMisses = true)
        panel.updateRule(updatedRule)

        panel.shouldBeBlue()
    }

    @Test
    fun `Should reset color when darts are cleared`() {
        val panel = DartzeeRuleVerificationPanel()

        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)

        panel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        panel.shouldBeRed()

        panel.btnReset.doClick()
        panel.shouldBeBlue()
    }

    @Test
    fun `Should update combinations text when darts are thrown`() {
        val panel = DartzeeRuleVerificationPanel()
        val rule = makeDartzeeRuleDto()
        rule.runStrengthCalculation()

        panel.updateRule(rule)
        panel.lblCombinations.text shouldBe "1 combinations (success%: 10.0%)"

        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))
        panel.lblCombinations.text shouldBe "1 combinations (success%: 20.0%)"
    }

    @Test
    fun `Should stop listening for clicks once three darts have been thrown, and listen again when reset is pressed`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val panel = DartzeeRuleVerificationPanel()
        val frame = JFrame()
        frame.size = Dimension(500, 500)
        frame.contentPane.add(panel)
        frame.isVisible = true
        flushEdt()

        val rule =
            makeDartzeeRuleDto(
                calculationResult = makeDartzeeRuleCalculationResult(getAllNonMissSegments())
            )
        panel.updateRule(rule)

        panel.dartboard.throwDartByClick()
        panel.dartboard.throwDartByClick()
        panel.dartboard.throwDartByClick()

        panel.dartsThrown.size shouldBe 3

        panel.dartboard.throwDartByClick()
        panel.dartsThrown.size shouldBe 3

        panel.btnReset.doClick()
        panel.dartsThrown.shouldBeEmpty()

        panel.dartboard.throwDartByClick()
        panel.dartsThrown.size shouldBe 1
    }

    private fun DartzeeRuleVerificationPanel.shouldBeBlue() {
        tfResult.foreground shouldBe Color.WHITE
        background shouldBe DartsColour.COLOUR_PASTEL_BLUE
    }

    private fun DartzeeRuleVerificationPanel.shouldBeRed() {
        tfResult.foreground shouldBe Color.RED
        background shouldBe DartsColour.getDarkenedColour(Color.RED)
    }

    private fun DartzeeRuleVerificationPanel.shouldBeGreen() {
        tfResult.foreground shouldBe Color.GREEN
        background shouldBe DartsColour.getDarkenedColour(Color.GREEN)
    }
}
