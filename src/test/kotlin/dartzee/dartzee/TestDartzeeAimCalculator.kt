package dartzee.dartzee

import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.ai.DELIBERATE_MISS
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.helper.AbstractTest
import dartzee.missTwenty
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.screen.Dartboard
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.DurationTimer
import dartzee.utils.getAllNonMissSegments
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Point
import javax.swing.ImageIcon
import javax.swing.JLabel

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

        val dartboard = DartzeeDartboard(400, 400)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(segmentStatus)

        val pt = calculator.getPointToAimFor(dartboard, segmentStatus, true)
        pt shouldBe DELIBERATE_MISS
    }

    @Test
    fun `Should deliberately miss if only valid segments are misses`()
    {
        val segmentStatus = SegmentStatus(listOf(missTwenty), listOf(missTwenty))

        val dartboard = DartzeeDartboard(400, 400)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(segmentStatus)

        val pt = calculator.getPointToAimFor(dartboard, segmentStatus, true)
        pt shouldBe DELIBERATE_MISS
    }

    @Test
    @Tag("integration")
    fun `Should be performant`()
    {
        val awkward = allNonMisses.filter { it.score != 25 }
        val segmentStatus = SegmentStatus(awkward, awkward)

        val dartboard = DartzeeDartboard(400, 400)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(segmentStatus)

        val timer = DurationTimer()
        repeat(10)
        {
            calculator.getPointToAimFor(dartboard, segmentStatus, true)
        }

        val timeElapsed = timer.getDuration()
        timeElapsed shouldBeLessThan 5000
    }

    private fun verifyAim(segmentStatus: SegmentStatus, screenshotName: String, aggressive: Boolean = false)
    {
        val dartboard = DartzeeDartboard(400, 400)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(segmentStatus)

        val pt = calculator.getPointToAimFor(dartboard, segmentStatus, aggressive)
        val lbl = dartboard.markPoints(listOf(pt))
        lbl.shouldMatchImage(screenshotName)
    }
}

fun Dartboard.markPoints(points: List<Point>): JLabel
{
    val img = dartboardImage!!

    val g = img.graphics as Graphics2D
    g.color = Color.BLUE
    g.stroke = BasicStroke(3f)
    points.forEach { pt ->
        g.drawLine(pt.x - 5, pt.y - 5, pt.x + 5, pt.y + 5)
        g.drawLine(pt.x - 5, pt.y + 5, pt.x + 5, pt.y - 5)
    }

    val lbl = JLabel(ImageIcon(img))
    lbl.size = Dimension(500, 500)
    lbl.repaint()
    return lbl
}