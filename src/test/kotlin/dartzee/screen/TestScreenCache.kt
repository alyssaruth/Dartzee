package dartzee.screen

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

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
        val scrn = ScreenCache.get<MenuScreen>()
        ScreenCache.emptyCache()
        val other = ScreenCache.get<MenuScreen>()

        scrn shouldNotBeSameInstanceAs other
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

    }
}