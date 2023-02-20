package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.screen.FocusableWindow
import dartzee.screen.ScreenCache
import java.awt.Frame
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.WindowConstants

abstract class AbstractDartsGameScreen : FocusableWindow(), WindowListener
{
    var haveLostFocus = false
    private var havePacked = false

    init
    {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        addWindowListener(this)
    }

    /**
     * Abstract fns
     */
    abstract fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    abstract fun fireAppearancePreferencesChanged()

    /**
     * Hook for when a GameId has been clicked and the screen is already visible.
     */
    open fun displayGame(gameId: String)
    {
        toFront()
        state = Frame.NORMAL
    }
    open fun startNextGameIfNecessary()
    {
        //Do nothing by default
    }
    fun packIfNecessary()
    {
        if (!havePacked)
        {
            pack()
            havePacked = true
        }
    }

    /**
     * WindowListener
     */
    override fun windowClosed(arg0: WindowEvent)
    {
        ScreenCache.removeDartsGameScreen(this)
    }
    override fun windowDeactivated(arg0: WindowEvent)
    {
        haveLostFocus = true
    }

    override fun windowActivated(arg0: WindowEvent){}
    override fun windowClosing(arg0: WindowEvent){}
    override fun windowDeiconified(arg0: WindowEvent){}
    override fun windowIconified(arg0: WindowEvent){}
    override fun windowOpened(arg0: WindowEvent){}
}