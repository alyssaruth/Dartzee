package dartzee.screen

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.doClick
import dartzee.helper.AbstractTest
import dartzee.helper.markPoints
import dartzee.listener.DartboardListener
import dartzee.logging.CODE_RENDERED_DARTBOARD
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.`object`.WIREFRAME_COLOUR_WRAPPER
import dartzee.utils.DartsColour
import dartzee.utils.getAverage
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Point

class TestDartboard: AbstractTest()
{
    @Test
    fun `Should do nothing if told to paint with invalid dimensions`()
    {
        val dartboard = Dartboard()
        dartboard.size = Dimension(-30, -100)

        shouldNotThrowAny {
            dartboard.paintDartboard()
        }
    }

    @Test
    fun `Should not paint the dartboard a second time if the dimensions are unchanged`()
    {
        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        clearLogs()

        dartboard.paintDartboard()
        verifyNoLogs(CODE_RENDERED_DARTBOARD)
    }

    @Test
    fun `Listening state should be preserved across repaints`()
    {
        val dartboard = Dartboard(100, 100)
        dartboard.ensureListening()

        dartboard.size = Dimension(150, 150)
        dartboard.paintDartboard()
        dartboard.isListening() shouldBe true

        dartboard.stopListening()
        dartboard.size = Dimension(100, 100)
        dartboard.paintDartboard()
        dartboard.isListening() shouldBe false
    }

    @Test
    fun `Dartboard listener should be notified if set`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboard()
        dartboard.ensureListening()

        val listener = mockk<DartboardListener>(relaxed = true)

        dartboard.addDartboardListener(listener)

        dartboard.doClick(25, 10)

        verify { listener.dartThrown(Dart(20, 1))}
    }

    @Test
    fun `Dartboard should paint the correct colours`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboard(colourWrapper = DEFAULT_COLOUR_WRAPPER)

        val img = dartboard.dartboardImage!!

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

    @Test
    fun `Should not explode if hovered over before painting has finished`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.ensureListening()

        shouldNotThrowAny {
            dartboard.highlightDartboard(dartboard.centerPoint)
        }
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - default`()
    {
        val dartboard = Dartboard(400, 400)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.shouldMatchImage("default")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - with numbers`()
    {
        val dartboard = Dartboard(500, 500)
        dartboard.renderScoreLabels = true
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.shouldMatchImage("scores")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - hovered`()
    {
        val dartboard = Dartboard(250, 250)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.ensureListening()

        val pt = dartboard.getPointsForSegment(1, SegmentType.OUTER_SINGLE).first()
        dartboard.highlightDartboard(pt)
        dartboard.shouldMatchImage("hovered")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - wireframe`()
    {
        val dartboard = Dartboard(250, 250)
        dartboard.paintDartboard(WIREFRAME_COLOUR_WRAPPER)
        dartboard.shouldMatchImage("wireframe")
    }

    @Test
    @Tag("screenshot")
    fun `Should get all the correct aim points`()
    {
        val dartboard = Dartboard(400, 400)
        dartboard.paintDartboard(WIREFRAME_COLOUR_WRAPPER)

        val pts = dartboard.getPotentialAimPoints().map { it.point }
        val lbl = dartboard.markPoints(pts)
        lbl.shouldMatchImage("aim points")
    }

    @Test
    @Tag("screenshot")
    fun `Should correctly scale up an AimPoint calculated from a smaller dartboard`()
    {
        val smallBoard = Dartboard(200, 200)
        smallBoard.paintDartboard()

        val bigBoard = Dartboard(400, 400)
        bigBoard.paintDartboard(WIREFRAME_COLOUR_WRAPPER)

        val smallPoints = smallBoard.getPotentialAimPoints()

        // Should be an identical image to the one from the above test
        val bigPoints = smallPoints.map { bigBoard.translateAimPoint(it) }
        val lbl = bigBoard.markPoints(bigPoints)
        lbl.shouldMatchImage("aim points")
    }

    @Test
    @Tag("screenshot")
    fun `Should correctly repaint at an enlarged or shrunk size`()
    {
        val board = Dartboard(200, 200)
        board.renderDarts = true
        board.paintDartboard()
        board.isVisible = true

        board.dartThrown(board.getPointForScore(20, SegmentType.INNER_SINGLE))
        board.dartThrown(board.getPointForScore(6, SegmentType.TREBLE))

        // Scale up
        board.size = Dimension(300, 300)
        board.paintDartboard()
        board.shouldMatchImage("scaledUp")

        // Scale down
        board.size = Dimension(150, 150)
        board.paintDartboard()
        board.shouldMatchImage("scaledDown")
    }

    private fun Dartboard.getPointForScore(score: Int, segmentType: SegmentType): Point
    {
        val points = getPointsForSegment(score, segmentType)
        return getAverage(points)
    }
}

