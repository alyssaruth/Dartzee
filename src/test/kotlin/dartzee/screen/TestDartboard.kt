package dartzee.screen

import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.`object`.ColourWrapper
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.Dart
import dartzee.core.helper.verifyNotCalled
import dartzee.dartzee.markPoints
import dartzee.doClick
import dartzee.helper.AbstractTest
import dartzee.listener.DartboardListener
import dartzee.usingTestDartboard
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
        usingTestDartboard { dartboard ->
            val listener = mockk<DartboardListener>(relaxed = true)

            dartboard.addDartboardListener(listener)

            dartboard.doClick(25, 10)

            verify { listener.dartThrown(Dart(20, 1)) }
        }
    }

    @Test
    fun `It should cache the image and re-use the cache for future paints`()
    {
        usingTestDartboard(paint = false) { dartboard ->
            Dartboard.dartboardTemplate shouldBe null

            val spied = spyk(dartboard)
            spied.paintDartboardCached()

            Dartboard.dartboardTemplate shouldNotBe null
            verifyNotCalled { spied.initialiseFromTemplate() }

            usingTestDartboard { dartboard2 ->
                val spied2 = spyk(dartboard2)
                spied2.paintDartboardCached()

                verify { spied2.initialiseFromTemplate() }
            }
        }
    }

    @Test
    fun `Dartboard template should be cleared when appearance preferences are changed`()
    {
        usingTestDartboard(paint = false) { dartboard ->
            dartboard.paintDartboardCached()

            Dartboard.appearancePreferenceChanged()

            Dartboard.dartboardTemplate shouldBe null
        }
    }


    @Test
    fun `Dartboard should paint the correct colours`()
    {
        usingTestDartboard(paint = false) { dartboard ->
            Dartboard.dartboardTemplate = null

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

    @Test
    fun `Should match snapshot - default`()
    {
        usingTestDartboard(250, 250, paint = false) { dartboard ->
            dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
            dartboard.shouldMatchImage("default")
        }
    }

    @Test
    fun `Should match snapshot - with numbers`()
    {
        usingTestDartboard(500, 500, paint = false) { dartboard ->
            dartboard.renderScoreLabels = true
            dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
            dartboard.shouldMatchImage("scores")
        }
    }

    @Test
    fun `Should match snapshot - wireframe`()
    {
        usingTestDartboard(250, 250, paint = false) { dartboard ->
            val colourWrapper = ColourWrapper(DartsColour.TRANSPARENT).also { it.edgeColour = Color.BLACK }
            dartboard.paintDartboard(colourWrapper, false)
            dartboard.shouldMatchImage("wireframe")
        }
    }

    @Test
    fun `Should get all the correct aim points`()
    {
        usingTestDartboard(400, 400, paint = false) { dartboard ->
            val colourWrapper = ColourWrapper(DartsColour.TRANSPARENT).also { it.edgeColour = Color.BLACK }
            dartboard.paintDartboard(colourWrapper, false)

            val pts = dartboard.getPotentialAimPoints().map { it.point }
            val lbl = dartboard.markPoints(pts)
            lbl.shouldMatchImage("aim points")
        }
    }

    @Test
    fun `Should correctly scale up an AimPoint calculated from a smaller dartboard`()
    {
        usingTestDartboard(200, 200) { smallBoard ->
            usingTestDartboard(400, 400, false) { bigBoard ->
                val colourWrapper = ColourWrapper(DartsColour.TRANSPARENT).also { it.edgeColour = Color.BLACK }
                bigBoard.paintDartboard(colourWrapper, false)

                val smallPoints = smallBoard.getPotentialAimPoints()

                // Should be an identical image to the one from the above test
                val bigPoints = smallPoints.map { bigBoard.translateAimPoint(it) }
                val lbl = bigBoard.markPoints(bigPoints)
                lbl.shouldMatchImage("aim points")
            }
        }
    }
}

