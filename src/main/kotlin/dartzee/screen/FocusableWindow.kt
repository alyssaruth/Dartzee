package dartzee.screen

import dartzee.logging.KEY_ACTIVE_WINDOW
import dartzee.utils.InjectedThings
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener
import javax.swing.JFrame

abstract class FocusableWindow: JFrame(), WindowFocusListener
{
    abstract val windowName: String

    init
    {
        addWindowFocusListener(this)
    }

    override fun windowGainedFocus(e: WindowEvent?)
    {
        InjectedThings.logger.addToContext(KEY_ACTIVE_WINDOW, windowName)
    }

    override fun windowLostFocus(e: WindowEvent?) {}
}