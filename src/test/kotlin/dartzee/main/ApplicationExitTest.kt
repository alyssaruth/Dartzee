package dartzee.main

import dartzee.getDialogMessage
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.assertDoesNotExit
import dartzee.helper.assertExits
import dartzee.logging.LogDestinationElasticsearch
import dartzee.runAsync
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import io.github.alyssaruth.swingtest.expectQuestionDialog
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ApplicationExitTest : AbstractTest() {
    @Test
    fun `Should exit without prompt if no games are open`() {
        assertExits(0) { exitApplication() }
    }

    @Test
    fun `Should not exit if there are open windows and user does not confirm`() {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        assertDoesNotExit {
            runAsync { exitApplication() }

            expectQuestionDialog("Are you sure you want to exit? There are 1 game window(s) still open.", "No")
        }
    }

    @Test
    fun `Should exit if there are open windows and user does confirm`() {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))
        ScreenCache.addDartsGameScreen("bar", mockk(relaxed = true))

        assertExits(0) {
            runAsync { exitApplication() }

            expectQuestionDialog("Are you sure you want to exit? There are 1 game window(s) still open.", "Yes")
        }
    }

    @Test
    fun `Should shut down the elasticsearch service`() {
        val mockEsDestination = mockk<LogDestinationElasticsearch>(relaxed = true)
        InjectedThings.esDestination = mockEsDestination

        assertExits(0) { exitApplication() }

        verify { mockEsDestination.shutDown() }
    }
}
