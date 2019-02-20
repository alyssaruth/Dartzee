package burlton.desktopcore.test.bean

import burlton.core.code.util.Debug
import burlton.desktopcore.code.bean.AbstractDevScreen
import burlton.desktopcore.code.bean.CheatBar
import burlton.desktopcore.code.screen.DebugConsole
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmptyString
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.Mockito.`when` as whenInvoke



class TestDevScreenCommands
{
    var commandsEnabled = true
    var toggle = false

    @Before
    fun setup()
    {
        Debug.initialise(DebugConsole())
    }

    @Test
    fun testCommandsDisabled()
    {
        commandsEnabled = false

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        assertFalse(cheatBar.isEnabled)
    }

    @Test
    fun testCommandsEnabled()
    {
        commandsEnabled = true

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        assertTrue(cheatBar.isEnabled)
    }

    private fun simulateKeyStroke(devScreen: AbstractDevScreen)
    {
        val cheatBarKeyStroke = devScreen.getKeyStrokeForCommandBar()
        val innerPanel = devScreen.contentPane as JPanel

        val keyEvent = mock(KeyEvent::class.java)
        val action = innerPanel.actionMap["showCheatBar"]

        SwingUtilities.notifyAction(action, cheatBarKeyStroke, keyEvent, this, keyEvent.modifiers)
    }

    @Test
    fun testTextualCommand()
    {
        commandsEnabled = true

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.text = "1+1"
        cheatBar.actionPerformed(mock(ActionEvent::class.java))

        assertThat(cheatBar.text, equalTo("2"))
        assertTrue(cheatBar.isEnabled)
    }

    @Test
    fun testStateChangingCommand()
    {
        commandsEnabled = true
        toggle = false

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.text = "SomethingElse"
        cheatBar.actionPerformed(mock(ActionEvent::class.java))

        assertThat(cheatBar.text, isEmptyString)
        assertFalse(cheatBar.isEnabled)
        assertTrue(toggle)
    }

    @Test
    fun testExceptionCommand()
    {
        commandsEnabled = true

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.text = "exception"
        cheatBar.actionPerformed(mock(ActionEvent::class.java))

        Debug.waitUntilLoggingFinished()

        assertThat(cheatBar.text, isEmptyString)
        assertFalse(cheatBar.isEnabled)
        assertThat(Debug.getLogs(), containsSubstring("java.lang.Exception: Test"))
    }


    inner class TestDevScreen(cheatBar: CheatBar): AbstractDevScreen(cheatBar)
    {
        override fun commandsEnabled(): Boolean
        {
            return commandsEnabled
        }

        override fun processCommand(cmd: String): String
        {
            if (cmd == "1+1")
            {
                return "2"
            }
            else if (cmd == "exception")
            {
                throw Exception("Test")
            }
            else
            {
                toggle = !toggle
                return ""
            }
        }
    }
}