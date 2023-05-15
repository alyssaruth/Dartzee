package dartzee.screen.dartzee

import com.github.alyssaburlton.swingtest.awaitCondition
import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.makeMouseEvent
import com.github.alyssaburlton.swingtest.toBufferedImage
import dartzee.bean.PresentationDartboard
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.doClick
import dartzee.getPointForSegment
import dartzee.helper.AbstractTest
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.DartsColour
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Point
import javax.swing.JButton

class TestDartboardSegmentSelectDialog : AbstractTest()
{
    private val segment = DartboardSegment(SegmentType.OUTER_SINGLE, 20)

    @Test
    fun `clicking the same segment should toggle it on and off`()
    {
        val (dlg, dartboard) = setup()

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)

        dlg.getSelection().shouldContainExactly(segment)

        dartboard.doClick(pt)
        dlg.getSelection().shouldBeEmpty()
    }

    @Test
    fun `should be able to select all`()
    {
        val allSegments = getAllNonMissSegments()
        val (dlg, dartboard) = setup()

        dlg.clickChild<JButton>(text = "Select All")
        dlg.getSelection() shouldBe allSegments.toSet()

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)
        dlg.clickChild<JButton>(text = "Select All")
        dlg.getSelection() shouldBe allSegments.toSet()
    }

    @Test
    fun `should be able to select none`()
    {
        val (dlg, dartboard) = setup()

        dlg.clickChild<JButton>(text = "Select None")
        dlg.getSelection().shouldBeEmpty()

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)
        dlg.clickChild<JButton>(text = "Select None")
        dlg.getSelection().shouldBeEmpty()
    }

    @Test
    fun `clicking outside the board should do nothing`()
    {
        val (dlg, dartboard) = setup()
        dartboard.doClick(Point(1, 1))
        dlg.getSelection().shouldBeEmpty()
    }

    @Test
    fun `dragging on the same segment should not toggle it again`()
    {
        val (dlg, dartboard) = setup()

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)
        dartboard.doDrag(pt.x, pt.y)

        dlg.getSelection().shouldContainExactly(segment)
    }

    @Test
    fun `dragging on a new segment should toggle it`()
    {
        val (dlg, dartboard) = setup()

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)

        val newPt = dartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 1))
        dartboard.doDrag(newPt.x, newPt.y)

        dlg.getSelection().shouldContainExactly(segment, DartboardSegment(SegmentType.OUTER_SINGLE, 1))
    }

    @Test
    fun `initial state should work correctly`()
    {
        val (dlg, dartboard) = setup(setOf(segment))
        dlg.getSelection().shouldContainExactlyInAnyOrder(segment)

        val pt = dartboard.getPointForSegment(segment)
        dartboard.toBufferedImage().getRGB(pt.x, pt.y) shouldBe DartsColour.DARTBOARD_BLACK.rgb
    }

    @Test
    fun `initial state should respond to drags`()
    {
        val (dlg, dartboard) = setup(setOf(segment))

        dlg.getSelection().shouldContainExactlyInAnyOrder(segment)

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doDrag(pt.x, pt.y)

        dlg.getSelection().shouldBeEmpty()
    }

    @Test
    fun `selecting segments should update their colour`()
    {
        val (_, dartboard) = setup()
        awaitCondition { dartboard.width > 0 }

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)
        dartboard.toBufferedImage().getRGB(pt.x, pt.y) shouldBe DartsColour.DARTBOARD_BLACK.rgb
    }

    @Test
    fun `cancelling should revert back to initial selection`()
    {
        val (dlg, dartboard) = setup(setOf(segment))

        val pt = dartboard.getPointForSegment(segment)
        dartboard.doClick(pt)
        dlg.getSelection().shouldBeEmpty()

        dlg.clickChild<JButton>(text = "Cancel")
        dlg.getSelection().shouldContainExactlyInAnyOrder(segment)
    }

    private fun setup(initialSelection: Set<DartboardSegment> = emptySet()): Pair<DartboardSegmentSelectDialog, PresentationDartboard>
    {
        val dlg = DartboardSegmentSelectDialog(initialSelection)
        dlg.isModal = false
        dlg.isVisible = true

        val dartboard = dlg.getChild<PresentationDartboard>()
        return dlg to dartboard
    }

    /**
     * TODO - Bake into swingtest
     */
    private fun Component.doDrag(x: Int = 0, y: Int = 0) {
        runOnEventThreadBlocking {
            val me = makeMouseEvent(this, x = x, y = y)
            mouseMotionListeners.forEach {
                it.mouseDragged(me)
            }
        }
    }
}