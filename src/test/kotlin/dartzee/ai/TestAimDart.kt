package dartzee.ai

import dartzee.helper.AbstractTest
import dartzee.`object`.SegmentType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAimDart : AbstractTest() {
    @Test
    fun `Should report the right segment type to aim for`() {
        AimDart(1, 0).getSegmentType() shouldBe SegmentType.MISS
        AimDart(1, 1).getSegmentType() shouldBe SegmentType.OUTER_SINGLE
        AimDart(1, 2).getSegmentType() shouldBe SegmentType.DOUBLE
        AimDart(1, 3).getSegmentType() shouldBe SegmentType.TREBLE
    }
}
