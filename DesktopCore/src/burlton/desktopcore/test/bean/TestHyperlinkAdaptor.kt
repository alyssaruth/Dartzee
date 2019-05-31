package burlton.desktopcore.test.bean

import burlton.desktopcore.code.bean.HyperlinkAdaptor
import burlton.desktopcore.code.bean.HyperlinkListener
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import burlton.desktopcore.test.helpers.MOUSE_EVENT_SINGLE_CLICK
import burlton.desktopcore.test.helpers.makeMouseEvent
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JPanel

class TestHyperlinkAdaptor: AbstractDesktopTest()
{
    @Test
    fun `Should not accept a non-component listener`()
    {
        shouldThrow<ClassCastException> {
            HyperlinkAdaptor(NonComponentHyperlinkListener())
        }
    }

    @Test
    fun `Should respond to mouse clicks`()
    {
        val listener = mockk<TestHyperlinkListener>(relaxed = true)

        val adaptor = HyperlinkAdaptor(listener)
        adaptor.mouseClicked(MOUSE_EVENT_SINGLE_CLICK)
        adaptor.mouseClicked(null)

        verifySequence{ listener.linkClicked(MOUSE_EVENT_SINGLE_CLICK); listener.linkClicked(null) }
    }

    @Test
    fun `Should change the cursor on mouse movement`()
    {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(makeMouseEvent())
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        adaptor.mouseMoved(MOUSE_EVENT_SINGLE_CLICK)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseMoved(null)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    @Test
    fun `Should revert the cursor on mouseExit`()
    {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(MOUSE_EVENT_SINGLE_CLICK)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseExited(null)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }
}

private class TestHyperlinkListener: JPanel(), HyperlinkListener
{
    override fun isOverHyperlink(arg0: MouseEvent?): Boolean
    {
        return arg0 === MOUSE_EVENT_SINGLE_CLICK
    }

    override fun linkClicked(arg0: MouseEvent?){}
}

private class NonComponentHyperlinkListener : HyperlinkListener
{
    override fun isOverHyperlink(arg0: MouseEvent?) = false
    override fun linkClicked(arg0: MouseEvent?){}
}