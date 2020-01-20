package burlton.dartzee.code.core.bean

import burlton.desktopcore.code.util.Debug
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.KeyStroke

abstract class AbstractDevScreen(private val commandBar: CheatBar) : JFrame()
{
    init
    {
        commandBar.setCheatListener(this)
    }

    /**
     * Abstract methods
     */
    abstract fun commandsEnabled(): Boolean
    abstract fun processCommand(cmd: String): String

    /**
     * Regular methods
     */
    fun enableCheatBar(enable: Boolean)
    {
        commandBar.isEnabled = enable
    }
    
    fun getKeyStrokeForCommandBar(): KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_MASK)

    fun processCommandWithTry(cmd: String): String
    {
        Debug.append("[Command Entered: $cmd]", true)

        var result = ""
        try
        {
            result = processCommand(cmd)
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
        }

        return result
    }
}
