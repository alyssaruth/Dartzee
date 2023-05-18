package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.game.SegmentStatuses
import dartzee.utils.getAllNonMissSegments
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

        dartboard.hoverSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1))
        dartboard.shouldMatchImage("hovered-1")

        dartboard.hoverSegment(DartboardSegment(SegmentType.DOUBLE, 25))
        dartboard.shouldMatchImage("hovered-bull")
    }

    @Test
    @Tag("screenshot")
    fun `Should support disabling and reenabling interaction`()
    {
        val dartboard = InteractiveDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)

        val pt = dartboard.getPointsForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1)).first()
        dartboard.highlightDartboard(pt)
        dartboard.stopInteraction()
        dartboard.highlightDartboard(pt)
        dartboard.shouldMatchImage("hover-diabled")

        dartboard.allowInteraction()
        dartboard.highlightDartboard(pt)
        dartboard.shouldMatchImage("hovered-1")
    }

    @Test
    @Tag("screenshot")
    fun `Hovering should interact correctly with segment statuses`()
    {
        val scoringSegments = getAllNonMissSegments().filter { it.getTotal() >= 20 }
        val validSegments = getAllNonMissSegments().filter { it.getTotal() >= 10 }
        val segmentStatuses = SegmentStatuses(scoringSegments, validSegments)

        val dartboard = InteractiveDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.updateSegmentStatus(segmentStatuses)

        dartboard.hoverSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1))
        dartboard.shouldMatchImage("hover-invalid-segment")

        dartboard.hoverSegment(DartboardSegment(SegmentType.INNER_SINGLE, 11))
        dartboard.shouldMatchImage("hover-valid-segment")

        dartboard.hoverSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))
        dartboard.shouldMatchImage("hover-scoring-segment")
    }

    private fun InteractiveDartboard.hoverSegment(segment: DartboardSegment)
    {
        val pt = getPointsForSegment(segment).first()
        highlightDartboard(pt)
    }
}