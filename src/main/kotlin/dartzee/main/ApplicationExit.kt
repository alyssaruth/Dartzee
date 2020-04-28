package dartzee.main

import dartzee.core.util.DialogUtil
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun exitApplication()
{
    val openGames = ScreenCache.getDartsGameScreens()
    val size = openGames.size
    if (size > 0)
    {
        val ans = DialogUtil.showQuestion("Are you sure you want to exit? There are $size game window(s) still open.", false)
        if (ans == JOptionPane.NO_OPTION)
        {
            return
        }
    }

    InjectedThings.esDestination.shutDown()

    exitProcess(0)
}
