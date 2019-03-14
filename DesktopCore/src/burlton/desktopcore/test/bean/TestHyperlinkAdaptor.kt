package burlton.desktopcore.test.bean

import burlton.desktopcore.code.bean.HyperlinkAdaptor
import burlton.desktopcore.code.bean.HyperlinkListener
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowAny
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JPanel

private val MOUSE_EVENT_OVER_HYPERLINK = mockk<MouseEvent>(relaxed = true)

class TestHyperlinkAdaptor
{
    @Test
    fun `Should not accept a non-component listener`()
    {
        shouldThrowAny {
            HyperlinkAdaptor(mockk())
        }
    }

    @Test
    fun `Should respond to mouse clicks`()
    {
        val mouseEvent = mockk<MouseEvent>(relaxed = true)

        val listener = mockk<TestHyperlinkListener>(relaxed = true)

        val adaptor = HyperlinkAdaptor(listener)
        adaptor.mouseClicked(mouseEvent)
        adaptor.mouseClicked(null)

        verifySequence{ listener.linkClicked(mouseEvent); listener.linkClicked(null) }
    }

    @Test
    fun `Should change the cursor on mouse movement`()
    {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(mockk(relaxed = true))
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        adaptor.mouseMoved(MOUSE_EVENT_OVER_HYPERLINK)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseMoved(null)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    @Test
    fun `Should revert the cursor on mouseExit`()
    {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(MOUSE_EVENT_OVER_HYPERLINK)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseExited(null)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }
}

private class TestHyperlinkListener: JPanel(), HyperlinkListener
{
    override fun isOverHyperlink(arg0: MouseEvent?): Boolean
    {
        return arg0 === MOUSE_EVENT_OVER_HYPERLINK
    }

    override fun linkClicked(arg0: MouseEvent?){}
}