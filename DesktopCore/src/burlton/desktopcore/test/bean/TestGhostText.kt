package burlton.desktopcore.test.bean

import burlton.dartzee.code.core.bean.GhostText
import burlton.desktopcore.test.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Font
import javax.swing.JTextField
import javax.swing.SwingConstants.LEADING

class TestGhostText: AbstractTest()
{
    @Test
    fun `Should construct with the right values`()
    {
        val label = JTextField()
        val font = Font("Tahoma", Font.BOLD, 25)
        label.font = font

        val ghostText = GhostText("Foo", label)
        ghostText.font shouldBe font
        ghostText.text shouldBe "Foo"
        ghostText.foreground.alpha shouldBe 127
        ghostText.horizontalAlignment shouldBe LEADING
    }

    @Test
    fun `Should be visible when text field has no text`()
    {
        val tf = JTextField()

        val ghostText = GhostText("Foo", tf)
        ghostText.isVisible shouldBe true
    }

    @Test
    fun `Should not be visible when text is entered, and should return when field is cleared`()
    {
        val tf = JTextField()

        val ghostText = GhostText("Foo", tf)
        tf.document.insertString(0, "some text", null)

        ghostText.isVisible shouldBe false

        tf.document.remove(0, tf.document.length)
        ghostText.isVisible shouldBe true
    }
}