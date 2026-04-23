package dartzee.theme

import com.github.alyssaburlton.swingtest.doHover
import com.github.alyssaburlton.swingtest.doHoverAway
import com.github.alyssaburlton.swingtest.makeMouseEvent
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import java.awt.Color
import javax.swing.JButton
import org.junit.jupiter.api.Test

class ButtonHoverListenerTest : AbstractTest() {
    @Test
    fun `Should update background accordingly`() {
        val button = JButton()
        button.background = Color.RED
        val listener = ButtonBackgroundUpdater(button)
        button.addMouseListener(listener)

        val lighter = Color.RED.brighter()
        val darker = Color.RED.darker()

        button.doHover()
        button.background shouldBe lighter

        button.doHoverAway()
        button.background shouldBe Color.RED

        listener.mousePressed(makeMouseEvent(button))
        button.background shouldBe darker

        listener.mouseReleased(makeMouseEvent(button))
        button.background shouldBe Color.RED
    }
}
