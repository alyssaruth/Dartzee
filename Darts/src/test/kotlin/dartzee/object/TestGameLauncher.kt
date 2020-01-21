package dartzee.test.`object`

import dartzee.`object`.GameLauncher
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestGameLauncher: AbstractTest()
{
    @Test
    fun `Should bring up the window when loading an already visible game`()
    {
        val scrn = mockk<AbstractDartsGameScreen>(relaxed = true)
        ScreenCache.addDartsGameScreen("foo", scrn)

        GameLauncher.loadAndDisplayGame("foo")

        verify { scrn.displayGame("foo") }
    }

    @Test
    fun `Should show an error and return if no game exists for the id`()
    {
        GameLauncher.loadAndDisplayGame("foo")

        dialogFactory.errorsShown.shouldContainExactly("Game foo does not exist.")
    }
}
