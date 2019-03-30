package burlton.dartzee.test.screen

import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent

class TestDartboard: AbstractTest()
{
    @Test
    fun `Dartboard listener should be notified if set`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)

        dartboard.addDartboardListener(listener)

        val me = mockk<MouseEvent>()

        every { me.point } returns Point(25, 10)

        dartboard.mouseClicked(me)

        verify { listener.dartThrown(Dart(20, 1))}
    }

    @Test
    fun `It should cache the image and re-use the cache for future paints`()
    {
        Dartboard.dartboardTemplate shouldBe null

        val dartboard = spyk(Dartboard(50, 50))
        dartboard.paintDartboardCached()

        Dartboard.dartboardTemplate shouldNotBe null
        verify(exactly = 0) { dartboard.initialiseFromTemplate() }

        val dartboard2 = spyk(Dartboard(50, 50))
        dartboard2.paintDartboardCached()

        verify {dartboard2.initialiseFromTemplate()}
    }

    @Test
    fun `Dartboard template should be cleared when appearance preferences are changed`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboardCached()

        Dartboard.appearancePreferenceChanged()

        Dartboard.dartboardTemplate shouldBe null
    }


    @Test
    fun `Dartboard should paint the correct colours`()
    {
        Dartboard.dartboardTemplate = null

        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboard(cached = true, colourWrapper = DEFAULT_COLOUR_WRAPPER)

        val img = Dartboard.dartboardTemplate!!.getDartboardImg()

        val singleTwenty = img.getRGB(25, 10)
        val singleSix = img.getRGB(40, 25)
        val trebleNineteen = img.getRGB(22, 42)
        val doubleTwenty = img.getRGB(25, 8)
        val miss = img.getRGB(25, 7)
        val missBoard = img.getRGB(0, 0)

        Color(singleTwenty) shouldBe Color.BLACK
        Color(singleSix) shouldBe Color.WHITE
        Color(trebleNineteen) shouldBe Color.GREEN
        Color(doubleTwenty) shouldBe Color.RED
        Color(miss) shouldBe Color.BLACK
        Color(missBoard) shouldBe Color.BLACK
    }
}

