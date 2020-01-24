package dartzee.core.bean

import dartzee.core.helper.MOUSE_EVENT_SINGLE_CLICK
import dartzee.core.helper.makeMouseEvent
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JPanel

class TestHyperlinkAdaptor: AbstractTest()
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
        adaptor.mouseClicked(makeMouseEvent())

        verifySequence{ listener.linkClicked(MOUSE_EVENT_SINGLE_CLICK); listener.linkClicked(any()) }
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

        adaptor.mouseMoved(makeMouseEvent())
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

private class TestHyperlinkListener: JPanel(), IHyperlinkListener
{
    override fun isOverHyperlink(arg0: MouseEvent): Boolean
    {
        return arg0 === MOUSE_EVENT_SINGLE_CLICK
    }

    override fun linkClicked(arg0: MouseEvent){}
}

private class NonComponentHyperlinkListener : IHyperlinkListener
{
    override fun isOverHyperlink(arg0: MouseEvent) = false
    override fun linkClicked(arg0: MouseEvent){}
}