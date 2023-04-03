package dartzee.ai

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.bean.PresentationDartboard
import dartzee.helper.AbstractTest
import dartzee.helper.markPoints
import dartzee.`object`.SegmentType
import getPointForScore
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestStrategyUtils: AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should aim at the average point for the relevant segment`()
    {
        val t20 = getPointForScore(AimDart(20, 3))
        val outerEleven = getPointForScore(11, SegmentType.OUTER_SINGLE)

        val presentationDartboard = PresentationDartboard()
        presentationDartboard.setBounds(0, 0, AI_DARTBOARD_WIDTH, AI_DARTBOARD_HEIGHT)
        val lbl = presentationDartboard.markPoints(listOf(t20, outerEleven))
        lbl.shouldMatchImage("centerPoints")
    }
}