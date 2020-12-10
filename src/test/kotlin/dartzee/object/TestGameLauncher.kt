package dartzee.`object`

import dartzee.helper.AbstractTest
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestGameLauncher: AbstractTest()
{
    @Test
    fun `Should bring up the window when loading an already visible game`()
    {
        val scrn = mockk<AbstractDartsGameScreen>(relaxed = true)
        ScreenCache.addDartsGameScreen("foo", scrn)

        GameLauncher().loadAndDisplayGame("foo")

        verify { scrn.displayGame("foo") }
    }

    @Test
    fun `Should show an error and return if no game exists for the id`()
    {
        GameLauncher().loadAndDisplayGame("foo")

        dialogFactory.errorsShown.shouldContainExactly("Game foo does not exist.")
    }
}
