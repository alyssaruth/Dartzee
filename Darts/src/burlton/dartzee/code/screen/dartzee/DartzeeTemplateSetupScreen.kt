package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.UtilitiesScreen

class DartzeeTemplateSetupScreen: EmbeddedScreen()
{
    override fun initialise()
    {

    }

    override fun getScreenName() = "Dartzee Templates"

    override fun getBackTarget() = ScreenCache.getScreen(UtilitiesScreen::class.java)
}