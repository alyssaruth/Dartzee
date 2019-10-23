package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.UtilitiesScreen
import burlton.desktopcore.code.bean.ScrollTable
import java.awt.BorderLayout
import javax.swing.JPanel

class DartzeeTemplateSetupScreen: EmbeddedScreen()
{
    private val scrollTable = ScrollTable()
    private val panelEast = JPanel()

    override fun initialise()
    {
        add(scrollTable)
        add(panelEast, BorderLayout.EAST)
    }

    override fun getScreenName() = "Dartzee Templates"

    override fun getBackTarget() = ScreenCache.getScreen(UtilitiesScreen::class.java)
}