package dartzee.ai

import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.dartzee.markPoints
import dartzee.helper.AbstractTest
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
        val lbl = AI_DARTBOARD.markPoints(listOf(t20, outerEleven))
        lbl.shouldMatchImage("centerPoints")
    }
}