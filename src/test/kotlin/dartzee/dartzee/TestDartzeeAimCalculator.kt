package dartzee.dartzee

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.ai.AI_DARTBOARD
import dartzee.ai.DELIBERATE_MISS
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.helper.AbstractTest
import dartzee.helper.markPoints
import dartzee.missTwenty
import dartzee.`object`.ComputationalDartboard
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.DurationTimer
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

private val allNonMisses = getAllNonMissSegments()
private val calculator = DartzeeAimCalculator()

class TestDartzeeAimCalculator: AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should aim at the bullseye for a fully valid dartboard`()
    {
        val segmentStatus = SegmentStatus(allNonMisses, allNonMisses)
        verifyAim(segmentStatus, "All valid")
    }

    @Test
    @Tag("screenshot")
    fun `Should aim at the right place for all odd`()
    {
        val odd = allNonMisses.filter { DartzeeDartRuleOdd().isValidSegment(it) }
        val segmentStatus = SegmentStatus(odd, odd)
        verifyAim(segmentStatus, "Odd")
    }

    @Test
    @Tag("screenshot")
    fun `Should aim based on valid segments for if cautious`()
    {
        val twenties = allNonMisses.filter { it.score == 20 }
        val segmentStatus = SegmentStatus(twenties, allNonMisses)
        verifyAim(segmentStatus, "Score 20s - cautious", false)
    }

    @Test
    @Tag("screenshot")
    fun `Should aim based on scoring segments if aggressive`()
    {
        val twenties = allNonMisses.filter { it.score == 20 }
        val segmentStatus = SegmentStatus(twenties, allNonMisses)
        verifyAim(segmentStatus, "Score 20s - aggressive", true)
    }

    @Test
    @Tag("screenshot")
    fun `Should go on score for tie breakers`()
    {
        val trebles = allNonMisses.filter { it.getMultiplier() == 3 }
        val segmentStatus = SegmentStatus(trebles, trebles)
        verifyAim(segmentStatus, "Trebles")

        val treblesWithoutTwenty = trebles.filter { it.score != 20 }
        verifyAim(SegmentStatus(treblesWithoutTwenty, treblesWithoutTwenty), "Trebles (no 20)")
    }

    @Test
    @Tag("screenshot")
    fun `Should aim correctly if bullseye is missing`()
    {
        val nonBull = allNonMisses.filter { it.getTotal() != 50 }
        val segmentStatus = SegmentStatus(nonBull, nonBull)
        verifyAim(segmentStatus, "No bullseye")
    }

    @Test
    @Tag("screenshot")
    fun `Should aim correctly for some missing trebles`()
    {
        val segments = allNonMisses.filterNot { it.getMultiplier() == 3 && (it.score == 20 || it.score == 3) }
        val segmentStatus = SegmentStatus(segments, segments)
        verifyAim(segmentStatus, "Missing trebles")
    }

    @Test
    @Tag("screenshot")
    fun `Should revert to aiming at valid segments if there are no scoring segments`()
    {
        val validSegments = allNonMisses.filter { it.score == 1 }
        val segmentStatus = SegmentStatus(emptyList(), validSegments)
        verifyAim(segmentStatus, "No scoring segments", true)
    }

    @Test
    fun `Should deliberately miss if no valid segments`()
    {
        val segmentStatus = SegmentStatus(emptyList(), emptyList())

        val pt = calculator.getPointToAimFor(AI_DARTBOARD, segmentStatus, true)
        pt shouldBe DELIBERATE_MISS
    }

    @Test
    fun `Should deliberately miss if only valid segments are misses`()
    {
        val segmentStatus = SegmentStatus(listOf(missTwenty), listOf(missTwenty))

        val pt = calculator.getPointToAimFor(AI_DARTBOARD, segmentStatus, true)
        pt shouldBe DELIBERATE_MISS
    }

    @Test
    @Tag("integration")
    fun `Should be performant`()
    {
        val awkward = allNonMisses.filter { it.score != 25 }
        val segmentStatus = SegmentStatus(awkward, awkward)

        val dartboard = ComputationalDartboard(400, 400)

        val timer = DurationTimer()
        repeat(10) {
            calculator.getPointToAimFor(dartboard, segmentStatus, true)
        }

        val timeElapsed = timer.getDuration()
        timeElapsed shouldBeLessThan 5000
    }

    private fun verifyAim(segmentStatus: SegmentStatus, screenshotName: String, aggressive: Boolean = false)
    {
        val dartboard = ComputationalDartboard(400, 400)
        val pt = calculator.getPointToAimFor(dartboard, segmentStatus, aggressive)

        val oldDartboard = DartzeeDartboard(400, 400)
        oldDartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        oldDartboard.refreshValidSegments(segmentStatus)

        val lbl = oldDartboard.markPoints(listOf(pt))
        lbl.shouldMatchImage(screenshotName)
    }
}