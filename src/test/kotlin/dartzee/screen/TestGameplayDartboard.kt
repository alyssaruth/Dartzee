package dartzee.screen

import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.bean.DartLabel
import dartzee.bean.InteractiveDartboard
import dartzee.core.helper.verifyNotCalled
import dartzee.getPointForSegment
import dartzee.helper.AbstractTest
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.game.FakeDartsScreen
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestGameplayDartboard : AbstractTest()
{
    @Test
    fun `Dartboard listener should be notified if set`()
    {
        val dartboard = factoryGameplayDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))
        interactiveDartboard.doClick(pt.x, pt.y)

        verify { listener.dartThrown(Dart(20, 1))}
    }

    @Test
    fun `Should support disabling and re-enabling interaction`()
    {
        val dartboard = factoryGameplayDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))

        dartboard.stopListening()
        interactiveDartboard.doClick(pt.x, pt.y)
        flushEdt()
        verifyNotCalled { listener.dartThrown(any()) }
        dartboard.findChild<DartLabel>().shouldBeNull()

        dartboard.ensureListening()
        interactiveDartboard.doClick(pt.x, pt.y)
        flushEdt()
        dartboard.findChild<DartLabel>().shouldNotBeNull()
        verify { listener.dartThrown(Dart(20, 1))}
    }

    @Test
    fun `Should suppress adding a dart on the first click if parent window has lost focus`()
    {
        val dartboard = factoryGameplayDartboard()
        val parent = FakeDartsScreen()
        parent.add(dartboard)
        parent.haveLostFocus = true

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))
        interactiveDartboard.doClick(pt.x, pt.y)
        flushEdt()

        parent.haveLostFocus shouldBe false
        dartboard.findChild<DartLabel>().shouldBeNull()

        interactiveDartboard.doClick(pt.x, pt.y)
        flushEdt()
        dartboard.findChild<DartLabel>().shouldNotBeNull()
    }

    @Test
    fun `Should support clearing darts`()
    {
        val dartboard = factoryGameplayDartboard()

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))

        interactiveDartboard.doClick(pt.x, pt.y)
        flushEdt()
        dartboard.findChild<DartLabel>().shouldNotBeNull()

        dartboard.clearDarts()
        dartboard.findChild<DartLabel>().shouldBeNull()
    }

    @Test
    fun `Should not render darts if too small`()
    {
        val dartboard = factoryGameplayDartboard()

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))

        interactiveDartboard.doClick(pt.x, pt.y)
        flushEdt()
        dartboard.findChild<DartLabel>().shouldNotBeNull()

        dartboard.setBounds(0, 0, 75, 75)
        flushEdt()
        dartboard.findChild<DartLabel>().shouldBeNull()

        dartboard.setBounds(0, 0, 400, 400)
        flushEdt()
        dartboard.findChild<DartLabel>().shouldNotBeNull()
    }

    @Test
    @Tag("screenshot")
    fun `Should handle layering darts on top of hover state`()
    {
        val dartboard = factoryGameplayDartboard()

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))

        interactiveDartboard.doClick(pt.x, pt.y)
        interactiveDartboard.highlightDartboard(pt)

        dartboard.shouldMatchImage("hovered-with-dart")
    }

    @Test
    @Tag("screenshot")
    fun `Should re-render darts in the right places on resize`()
    {
        val dartboard = factoryGameplayDartboard()

        val interactiveDartboard = dartboard.getChild<InteractiveDartboard>()
        val pt1 = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.OUTER_SINGLE, 20))
        val pt2 = interactiveDartboard.getPointForSegment(DartboardSegment(SegmentType.DOUBLE, 14))

        interactiveDartboard.doClick(pt1.x, pt1.y)
        interactiveDartboard.doClick(pt2.x, pt2.y)
        flushEdt()

        dartboard.shouldMatchImage("darts-original-size")

        dartboard.setBounds(0, 0, 150, 150)
        flushEdt()
        dartboard.shouldMatchImage("darts-resized-smaller")

        dartboard.setBounds(0, 0, 400, 400)
        flushEdt()
        dartboard.shouldMatchImage("darts-original-size")
    }

    private fun factoryGameplayDartboard(): GameplayDartboard {
        val dartboard = GameplayDartboard()
        dartboard.setBounds(0, 0, 400, 400)
        flushEdt()
        return dartboard
    }
}