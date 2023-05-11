package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestInteractiveDartboard : AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should match snapshot - hovered`()
    {
        val dartboard = InteractiveDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)

        val pt = dartboard.getPointsForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1)).first()
        dartboard.highlightDartboard(pt)
        dartboard.shouldMatchImage("hovered-1")

        val pt2 = dartboard.getPointsForSegment(DartboardSegment(SegmentType.DOUBLE, 25)).first()
        dartboard.highlightDartboard(pt2)
        dartboard.shouldMatchImage("hovered-bull")
    }

    @Test
    @Tag("screenshot")
    fun `Should support disabling and reenabling interaction`()
    {
        val dartboard = InteractiveDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)

        val pt = dartboard.getPointsForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1)).first()

        dartboard.stopInteraction()
        dartboard.highlightDartboard(pt)
        dartboard.shouldMatchImage("hover-diabled")

        dartboard.allowInteraction()
        dartboard.highlightDartboard(pt)
        dartboard.shouldMatchImage("hovered-1")
    }
}