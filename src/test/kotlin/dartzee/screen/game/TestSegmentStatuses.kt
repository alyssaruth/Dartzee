package dartzee.screen.game

import dartzee.helper.AbstractTest
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSegmentStatuses : AbstractTest()
{
    @Test
    fun `Should report the correct status for a segment`()
    {
        val validSegments = getAllNonMissSegments().filter { it.score > 10 }
        val scoringSegments = validSegments.filter { it.score >= 20 }

        val segmentStatuses = SegmentStatuses(scoringSegments, validSegments)
        segmentStatuses.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 20)) shouldBe SegmentStatus.SCORING
        segmentStatuses.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 15)) shouldBe SegmentStatus.VALID
        segmentStatuses.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 1)) shouldBe SegmentStatus.INVALID

        val nullStatus: SegmentStatuses? = null
        nullStatus.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 1)) shouldBe SegmentStatus.SCORING
    }

}