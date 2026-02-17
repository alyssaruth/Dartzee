package dartzee.core.bean

import com.github.alyssaburlton.swingtest.makeMouseEvent
import dartzee.helper.AbstractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verifySequence
import org.junit.jupiter.api.Test
import java.awt.Cursor
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel

private val mouseEventOverLink = makeMouseEvent(JButton())
private val mouseEventNotOverLink = makeMouseEvent(JButton())

class TestHyperlinkAdaptor : AbstractTest() {
    @Test
    fun `Should not accept a non-component listener`() {
        shouldThrow<ClassCastException> { HyperlinkAdaptor(NonComponentHyperlinkListener()) }
    }

    @Test
    fun `Should respond to mouse clicks`() {
        val listener = spyk<TestHyperlinkListener>()

        val adaptor = HyperlinkAdaptor(listener)
        adaptor.mouseClicked(mouseEventOverLink)
        adaptor.mouseClicked(mouseEventNotOverLink)

        verifySequence {
            listener.linkClicked(mouseEventOverLink)
            listener.linkClicked(mouseEventNotOverLink)
        }
    }

    @Test
    fun `Should change the cursor on mouse movement`() {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(mouseEventNotOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        adaptor.mouseMoved(mouseEventOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseMoved(mouseEventNotOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        adaptor.mouseEntered(mouseEventNotOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

        adaptor.mouseEntered(mouseEventOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    @Test
    fun `Should revert the cursor on mouseExit`() {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(mouseEventOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseExited(null)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    @Test
    fun `Should revert the cursor on mouseDragged`() {
        val listener = TestHyperlinkListener()
        val adaptor = HyperlinkAdaptor(listener)

        adaptor.mouseMoved(mouseEventOverLink)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        adaptor.mouseDragged(null)
        listener.cursor shouldBe Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }
}

private class TestHyperlinkListener : JPanel(), IHyperlinkListener {
    override fun isOverHyperlink(arg0: MouseEvent) = arg0 === mouseEventOverLink

    override fun linkClicked(arg0: MouseEvent) {}
}

private class NonComponentHyperlinkListener : IHyperlinkListener {
    override fun isOverHyperlink(arg0: MouseEvent) = false

    override fun linkClicked(arg0: MouseEvent) {}
}
