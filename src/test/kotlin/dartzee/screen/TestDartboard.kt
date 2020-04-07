package dartzee.screen

import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.Dart
import dartzee.core.helper.verifyNotCalled
import dartzee.doClick
import dartzee.helper.AbstractTest
import dartzee.listener.DartboardListener
import dartzee.utils.DartsColour
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import java.awt.Color

class TestDartboard: AbstractTest()
{
    @Test
    fun `Dartboard listener should be notified if set`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)

        dartboard.addDartboardListener(listener)

        dartboard.doClick(25, 10)

        verify { listener.dartThrown(Dart(20, 1))}
    }

    @Test
    fun `It should cache the image and re-use the cache for future paints`()
    {
        Dartboard.dartboardTemplate shouldBe null

        val dartboard = spyk(Dartboard(50, 50))
        dartboard.paintDartboardCached()

        Dartboard.dartboardTemplate shouldNotBe null
        verifyNotCalled { dartboard.initialiseFromTemplate() }

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

        Color(singleTwenty) shouldBe DartsColour.DARTBOARD_BLACK
        Color(singleSix) shouldBe Color.WHITE
        Color(trebleNineteen) shouldBe Color.GREEN
        Color(doubleTwenty) shouldBe Color.RED
        Color(miss) shouldBe Color.BLACK
        Color(missBoard) shouldBe Color.BLACK
    }
}

