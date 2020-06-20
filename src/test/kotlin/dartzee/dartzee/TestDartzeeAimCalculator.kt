package dartzee.dartzee

import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.helper.AbstractTest
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.getAllPossibleSegments
import org.junit.Test
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import javax.swing.ImageIcon
import javax.swing.JLabel

class TestDartzeeAimCalculator: AbstractTest()
{
    private val allNonMisses = getAllPossibleSegments().filter { !it.isMiss() }

    @Test
    fun `Should aim at the bullseye for a fully valid dartboard`()
    {
        val segmentStatus = SegmentStatus(allNonMisses, allNonMisses)
        verifyAim(segmentStatus, "All valid")
    }

    @Test
    fun `Should aim at the right place for all odd`()
    {
        val odd = allNonMisses.filter { DartzeeDartRuleOdd().isValidSegment(it) }
        val segmentStatus = SegmentStatus(odd, odd)
        verifyAim(segmentStatus, "Odd")
    }

    @Test
    fun `Should aim based on valid segments for if cautious`()
    {
        val twenties = allNonMisses.filter { it.score == 20 }
        val segmentStatus = SegmentStatus(twenties, allNonMisses)
        verifyAim(segmentStatus, "Score 20s - cautious", false)
    }

    @Test
    fun `Should aim based on scoring segments if aggressive`()
    {
        val twenties = allNonMisses.filter { it.score == 20 }
        val segmentStatus = SegmentStatus(twenties, allNonMisses)
        verifyAim(segmentStatus, "Score 20s - aggressive", true)
    }

    @Test
    fun `Should go on score for tie breakers`()
    {
        val trebles = allNonMisses.filter { it.getMultiplier() == 3 }
        val segmentStatus = SegmentStatus(trebles, trebles)
        verifyAim(segmentStatus, "Trebles")

        val treblesWithoutTwenty = trebles.filter { it.score != 20 }
        verifyAim(SegmentStatus(treblesWithoutTwenty, treblesWithoutTwenty), "Trebles (no 20)")
    }

    private fun verifyAim(segmentStatus: SegmentStatus, screenshotName: String, aggressive: Boolean = false)
    {
        val dartboard = DartzeeDartboard(400, 400)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(segmentStatus)

        val pt = DartzeeAimCalculator().getPointToAimFor(dartboard, segmentStatus, aggressive)
        val img = dartboard.dartboardImage!!

        val g = img.graphics as Graphics2D
        g.color = Color.BLUE
        g.stroke = BasicStroke(3f)
        g.drawLine(pt.x - 5, pt.y - 5, pt.x + 5, pt.y + 5)
        g.drawLine(pt.x - 5, pt.y + 5, pt.x + 5, pt.y - 5)

        val lbl = JLabel(ImageIcon(img))
        lbl.size = Dimension(500, 500)
        lbl.repaint()
        lbl.shouldMatchImage(screenshotName)
    }
}
