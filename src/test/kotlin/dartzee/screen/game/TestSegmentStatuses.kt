package dartzee.screen.game

import dartzee.doubleTwenty
import dartzee.helper.AbstractTest
import dartzee.missSeventeen
import dartzee.missTwenty
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.singleTwenty
import dartzee.trebleTwenty
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSegmentStatuses : AbstractTest() {
    @Test
    fun `Should report the correct status for a segment`() {
        val validSegments = getAllNonMissSegments().filter { it.score > 10 }
        val scoringSegments = validSegments.filter { it.score >= 20 }

        val segmentStatuses = SegmentStatuses(scoringSegments, validSegments)
        segmentStatuses.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 20)) shouldBe
            SegmentStatus.SCORING
        segmentStatuses.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 15)) shouldBe
            SegmentStatus.VALID
        segmentStatuses.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 1)) shouldBe
            SegmentStatus.INVALID

        val nullStatus: SegmentStatuses? = null
        nullStatus.getSegmentStatus(DartboardSegment(SegmentType.OUTER_SINGLE, 1)) shouldBe
            SegmentStatus.SCORING
    }

    @Test
    fun `Should correctly report whether missing is allowed`() {
        SegmentStatuses(emptyList(), listOf(missTwenty)).allowsMissing() shouldBe true
        SegmentStatuses(listOf(missSeventeen), listOf(missSeventeen)).allowsMissing() shouldBe true
        SegmentStatuses(listOf(), listOf(singleTwenty, doubleTwenty, trebleTwenty))
            .allowsMissing() shouldBe false
    }
}
