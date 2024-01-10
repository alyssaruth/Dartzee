package dartzee.screen

import dartzee.helper.AbstractTest
import dartzee.helper.logger
import dartzee.logging.KEY_ACTIVE_WINDOW
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TestFocusableWindow : AbstractTest() {
    @Test
    fun `Should update logging context when gains focus`() {
        val window = FakeFocusableWindow()

        logger.addToContext(KEY_ACTIVE_WINDOW, "Foo")
        window.windowGainedFocus(mockk())
        logger.loggingContext[KEY_ACTIVE_WINDOW] shouldBe "Fake Window"
    }

    private inner class FakeFocusableWindow : FocusableWindow() {
        override val windowName = "Fake Window"
    }
}
