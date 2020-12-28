package dartzee.main

import dartzee.helper.AbstractTest
import dartzee.helper.assertDoesNotExit
import dartzee.helper.assertExits
import dartzee.logging.LogDestinationElasticsearch
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import javax.swing.JOptionPane

class TestApplicationExit: AbstractTest()
{
    @Test
    fun `Should exit without prompt if no games are open`()
    {
        assertExits(0) {
            exitApplication()
        }

        dialogFactory.questionsShown.shouldBeEmpty()
    }

    @Test
    fun `Should not exit if there are open windows and user does not confirm`()
    {
        dialogFactory.questionOption = JOptionPane.NO_OPTION
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        assertDoesNotExit {
            exitApplication()
        }

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to exit? There are 1 game window(s) still open.")
    }

    @Test
    fun `Should exit if there are open windows and user does confirms`()
    {
        dialogFactory.questionOption = JOptionPane.YES_OPTION
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        assertExits(0) {
            exitApplication()
        }

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to exit? There are 1 game window(s) still open.")
    }

    @Test
    fun `Should shut down the elasticsearch service`()
    {
        val mockEsDestination = mockk<LogDestinationElasticsearch>(relaxed = true)
        InjectedThings.esDestination = mockEsDestination

        assertExits(0) {
            exitApplication()
        }

        verify { mockEsDestination.shutDown() }
    }
}