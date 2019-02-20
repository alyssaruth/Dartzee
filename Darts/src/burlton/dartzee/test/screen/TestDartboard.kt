package burlton.dartzee.test.screen

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.mockito.Mockito.`when` as whenInvoke

class TestDartboard
{
    @Before
    fun setup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
    }

    @Test
    fun `Dartboard listener should be notified if set`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboard()

        val listener = mock(DartboardListener::class.java)
        dartboard.addDartboardListener(listener)

        val me = mock(MouseEvent::class.java)
        whenInvoke(me.point).thenReturn(Point(25, 10))

        dartboard.mouseClicked(me)

        verify(listener, times(1)).dartThrown(Dart(20, 1))
    }

    @Test
    fun `It should cache the image and re-use the cache for future paints`()
    {
        assertNull(Dartboard.dartboardTemplate)

        val dartboard = spy(Dartboard(50, 50))
        dartboard.paintDartboardCached()

        assertNotNull(Dartboard.dartboardTemplate)
        verify(dartboard, never()).initialiseFromTemplate()

        val dartboard2 = spy(Dartboard(50, 50))
        dartboard2.paintDartboardCached()

        verify(dartboard2, times(1)).initialiseFromTemplate()
    }

    @Test
    fun `Dartboard template should be cleared when appearance preferences are changed`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboardCached()

        Dartboard.appearancePreferenceChanged()

        assertNull(Dartboard.dartboardTemplate)
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

        assertThat(Color(singleTwenty), equalTo(Color.BLACK))
        assertThat(Color(singleSix), equalTo(Color.WHITE))
        assertThat(Color(trebleNineteen), equalTo(Color.GREEN))
        assertThat(Color(doubleTwenty), equalTo(Color.RED))
        assertThat(Color(miss), equalTo(Color.BLACK))
        assertThat(Color(missBoard), equalTo(Color.BLACK))
    }
}

