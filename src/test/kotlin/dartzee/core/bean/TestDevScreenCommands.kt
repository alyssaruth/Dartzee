package dartzee.core.bean

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_COMMAND_ERROR
import dartzee.logging.Severity
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import java.awt.event.KeyEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class TestDevScreenCommands: AbstractTest()
{
    var commandsEnabled = true
    var toggle = false

    @Test
    fun testCommandsDisabled()
    {
        commandsEnabled = false

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.isVisible shouldBe false
    }

    @Test
    fun testCommandsEnabled()
    {
        commandsEnabled = true

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.isVisible shouldBe true
    }

    private fun simulateKeyStroke(devScreen: AbstractDevScreen)
    {
        val cheatBarKeyStroke = devScreen.getKeyStrokeForCommandBar()
        val innerPanel = devScreen.contentPane as JPanel

        val keyEvent = mockk<KeyEvent>(relaxed = true)
        val action = innerPanel.actionMap["showCheatBar"]

        SwingUtilities.notifyAction(action, cheatBarKeyStroke, keyEvent, this, keyEvent.modifiersEx)
    }

    @Test
    fun testTextualCommand()
    {
        commandsEnabled = true

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.text = "1+1"
        cheatBar.actionPerformed(mockk())

        cheatBar.text shouldBe "2"
        cheatBar.isVisible shouldBe true
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
        cheatBar.actionPerformed(mockk())

        cheatBar.text.shouldBeEmpty()
        cheatBar.isVisible.shouldBeFalse()
        toggle.shouldBeTrue()
    }

    @Test
    fun testExceptionCommand()
    {
        commandsEnabled = true

        val cheatBar = CheatBar()
        val devScreen = TestDevScreen(cheatBar)

        simulateKeyStroke(devScreen)

        cheatBar.text = "exception"
        cheatBar.actionPerformed(mockk())

        cheatBar.text.shouldBeEmpty()
        cheatBar.isVisible.shouldBeFalse()

        val log = verifyLog(CODE_COMMAND_ERROR, Severity.ERROR)
        log.errorObject?.message shouldBe "Test"
    }


    inner class TestDevScreen(cheatBar: CheatBar): AbstractDevScreen(cheatBar)
    {
        override val windowName = "Test"

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