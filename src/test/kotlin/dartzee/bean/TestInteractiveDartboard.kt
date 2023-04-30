package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel

class TestInteractiveDartboard : AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should match snapshot - hovered`()
    {
        val img = BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB)

        val dartboard = InteractiveDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.paint(img.graphics)

        val pt = dartboard.getPointsForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1)).first()
        dartboard.highlightDartboard(pt, img.graphics)
        img.toLabel().shouldMatchImage("hovered-1")

        val pt2 = dartboard.getPointsForSegment(DartboardSegment(SegmentType.DOUBLE, 25)).first()
        dartboard.highlightDartboard(pt2, img.graphics)
        img.toLabel().shouldMatchImage("hovered-bull")
    }

    private fun BufferedImage.toLabel() = JLabel(ImageIcon(this)).also { it.setSize(width, height) }
}