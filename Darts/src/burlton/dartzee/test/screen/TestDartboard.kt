package burlton.dartzee.test.screen

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
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
        verify(dartboard, never()).initialiseFromTemplate(anyObject())

        val dartboard2 = spy(Dartboard(50, 50))
        dartboard2.paintDartboardCached()

        verify(dartboard2).initialiseFromTemplate(anyObject())
    }

    @Test
    fun `Dartboard template should be cleared when appearance preferences are changed`()
    {
        val dartboard = Dartboard(50, 50)
        dartboard.paintDartboardCached()

        Dartboard.appearancePreferenceChanged()

        assertNull(Dartboard.dartboardTemplate)
    }
}

