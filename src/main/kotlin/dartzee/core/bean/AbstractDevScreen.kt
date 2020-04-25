package dartzee.core.bean

import dartzee.logging.CODE_COMMAND_ENTERED
import dartzee.logging.CODE_COMMAND_ERROR
import dartzee.utils.InjectedThings.logger
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
        commandBar.isVisible = enable
        repaint()
        revalidate()
    }
    
    fun getKeyStrokeForCommandBar(): KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_MASK)

    fun processCommandWithTry(cmd: String): String
    {
        logger.info(CODE_COMMAND_ENTERED, "Command entered: [$cmd]")

        return try
        {
            processCommand(cmd)
        }
        catch (t: Throwable)
        {
            logger.error(CODE_COMMAND_ERROR, "Error running command $cmd", t)
            ""
        }
    }
}
