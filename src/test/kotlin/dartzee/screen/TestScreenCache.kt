package dartzee.screen

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import dartzee.screen.game.DartsGameScreen
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestScreenCache: AbstractTest()
{
    @Test
    fun `Should construct an instance and return it on subsequent calls`()
    {
        val scrn = ScreenCache.get<MenuScreen>()
        scrn.shouldNotBeNull()

        val other = ScreenCache.get<MenuScreen>()
        other shouldBeSameInstanceAs  scrn
    }

    @Test
    fun `Should clear the cache`()
    {
        ScreenCache.addDartsGameScreen("Game 1", mockk())
        val scrn = ScreenCache.get<MenuScreen>()
        ScreenCache.emptyCache()
        val other = ScreenCache.get<MenuScreen>()

        scrn shouldNotBeSameInstanceAs other
        ScreenCache.getDartsGameScreen("Game 1") shouldBe null
    }

    @Test
    fun `Should switch the current screen to the new one and init it`()
    {
        val scrn = mockk<EmbeddedScreen>(relaxed = true)

        ScreenCache.switch(scrn)
        ScreenCache.currentScreen() shouldBeSameInstanceAs scrn

        verify { scrn.initialise() }
    }

    @Test
    fun `Should not init the switched screen if specified`()
    {
        val scrn = mockk<EmbeddedScreen>(relaxed = true)

        ScreenCache.switch(scrn, false)
        ScreenCache.currentScreen() shouldBeSameInstanceAs scrn

        verifyNotCalled { scrn.initialise() }
    }

    @Test
    fun `Should support switching by type`()
    {
        val scrn = ScreenCache.get<MenuScreen>()
        ScreenCache.switch<MenuScreen>()

        ScreenCache.currentScreen() shouldBeSameInstanceAs scrn
    }

    @Test
    fun `Should return the unique count of darts game screens`()
    {
        val screenOne = mockk<DartsGameScreen>(relaxed = true)
        val screenTwo = mockk<DartsGameScreen>(relaxed = true)

        ScreenCache.addDartsGameScreen("Game 1", screenOne)
        ScreenCache.addDartsGameScreen("Game 2", screenOne)
        ScreenCache.addDartsGameScreen("Game 3", screenTwo)

        val screens = ScreenCache.getDartsGameScreens()
        screens.size shouldBe 2
        screens.shouldContainExactly(screenOne, screenTwo)
    }

    @Test
    fun `Should be able to get the screen for a particular GameId`()
    {
        ScreenCache.getDartsGameScreen("Game 1") shouldBe null

        val screenOne = mockk<DartsGameScreen>(relaxed = true)
        ScreenCache.addDartsGameScreen("Game 1", screenOne)
        ScreenCache.getDartsGameScreen("Game 1") shouldBe screenOne
    }

    @Test
    fun `Should be able to remove a screen`()
    {
        val screenOne = mockk<DartsGameScreen>(relaxed = true)
        val screenTwo = mockk<DartsGameScreen>(relaxed = true)

        ScreenCache.addDartsGameScreen("Game 1", screenOne)
        ScreenCache.addDartsGameScreen("Game 2", screenOne)
        ScreenCache.addDartsGameScreen("Game 3", screenTwo)

        ScreenCache.removeDartsGameScreen(screenOne)

        ScreenCache.getDartsGameScreen("Game 1") shouldBe null
        ScreenCache.getDartsGameScreen("Game 2") shouldBe null
        ScreenCache.getDartsGameScreen("Game 3") shouldBe screenTwo
    }
}