package dartzee.logging

import com.github.alyssaburlton.swingtest.flushEdt
import dartzee.core.util.getAllChildComponentsForType
import dartzee.helper.AbstractTest
import dartzee.makeLogRecord
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.awt.Color
import javax.swing.JLabel
import javax.swing.text.StyleConstants

class TestLoggingConsole: AbstractTest()
{
    @Test
    fun `Should separate log records with a new line`()
    {
        val recordOne = makeLogRecord(loggingCode = LoggingCode("foo"), message = "log one")
        val recordTwo = makeLogRecord(loggingCode = LoggingCode("bar"), message = "log two")

        val console = LoggingConsole()
        console.log(recordOne)
        console.log(recordTwo)

        val text = console.getText()
        text shouldBe "\n$recordOne\n$recordTwo"
    }

    @Test
    fun `Should log a regular INFO log in green`()
    {
        val console = LoggingConsole()
        val infoLog = makeLogRecord(severity = Severity.INFO)

        console.log(infoLog)
        console.getTextColour() shouldBe Color.GREEN
    }

    @Test
    fun `Should log an ERROR log in red`()
    {
        val console = LoggingConsole()
        val errorLog = makeLogRecord(severity = Severity.ERROR)

        console.log(errorLog)
        console.getTextColour() shouldBe Color.RED
    }

    @Test
    fun `Should log the error message and stack trace`()
    {
        val console = LoggingConsole()
        val t = Throwable("Boom")

        val errorLog = makeLogRecord(severity = Severity.ERROR, message = "Failed to load screen", errorObject = t)
        console.log(errorLog)

        console.getText() shouldContain "Failed to load screen"
        console.getText() shouldContain "java.lang.Throwable: Boom"

        val endColour = console.getTextColour(console.doc.length - 1)
        endColour shouldBe Color.RED
    }

    @Test
    fun `Should log thread stacks`()
    {
        val console = LoggingConsole()

        val threadStackLock = makeLogRecord(severity = Severity.INFO, message = "AWT Thread", keyValuePairs = mapOf(KEY_STACK to "at Foo.bar(58)"))
        console.log(threadStackLock)

        console.getText() shouldContain "AWT Thread"
        console.getText() shouldContain "at Foo.bar(58)"
    }

    @Test
    fun `Should log SELECT statements in blue`()
    {
        val console = LoggingConsole()
        val record = makeLogRecord(loggingCode = CODE_SQL, message = "SELECT * FROM Game")
        console.log(record)

        console.getTextColour() shouldBe Color.CYAN
    }

    @Test
    fun `Should log INSERT statements in orange`()
    {
        val console = LoggingConsole()
        val record = makeLogRecord(loggingCode = CODE_SQL, message = "INSERT INTO Game")
        console.log(record)

        console.getTextColour() shouldBe Color.ORANGE
    }

    @Test
    fun `Should log UPDATE statements in orange`()
    {
        val console = LoggingConsole()
        val record = makeLogRecord(loggingCode = CODE_SQL, message = "UPDATE Game")
        console.log(record)

        console.getTextColour() shouldBe Color.ORANGE
    }

    @Test
    fun `Should log DELETE statements in pink`()
    {
        val console = LoggingConsole()
        val record = makeLogRecord(loggingCode = CODE_SQL, message = "DELETE FROM Game")
        console.log(record)

        console.getTextColour() shouldBe Color.PINK
    }

    @Test
    fun `Should log SQL run on a different DB with a grey background`()
    {
        val console = LoggingConsole()
        val record = makeLogRecord(loggingCode = CODE_SQL, message = "DELETE FROM Game", keyValuePairs = mapOf(KEY_DATABASE_NAME to "otherDb"))
        console.log(record)

        console.getBackgroundColour() shouldBe Color.DARK_GRAY
    }

    @Test
    fun `Should scroll to the bottom when a new log is added`()
    {
        val console = LoggingConsole()
        console.pack()
        console.scrollPane.verticalScrollBar.value shouldBe 0

        repeat(50)
        {
            console.log(makeLogRecord())
        }

        flushEdt()
        console.scrollPane.verticalScrollBar.value shouldBeGreaterThan 0
    }

    @Test
    fun `Should support clearing the logs`()
    {
        val console = LoggingConsole()
        console.log(makeLogRecord())

        console.clear()

        console.getText() shouldBe ""
    }

    @Test
    fun `Should update when logging context changes`()
    {
        val console = LoggingConsole()
        console.contextUpdated(mapOf())
        console.getAllChildComponentsForType<JLabel>().shouldBeEmpty()

        console.contextUpdated(mapOf("appVersion" to "4.1.1"))
        flushEdt()
        val labels = console.getAllChildComponentsForType<JLabel>()
        labels.size shouldBe 1
        labels.first().text shouldBe "appVersion: 4.1.1"

        console.contextUpdated(mapOf("appVersion" to "4.1.1", "devMode" to false))
        flushEdt()
        val newLabels = console.getAllChildComponentsForType<JLabel>()
        newLabels.map { it.text }.shouldContainExactly("appVersion: 4.1.1", "devMode: false")
    }

    private fun LoggingConsole.getText(): String =
        try
        {
            doc.getText(0, doc.length)
        }
        catch (t: Throwable)
        {
            ""
        }

    private fun LoggingConsole.getTextColour(position: Int = 0): Color
    {
        val style = doc.getCharacterElement(position)
        return StyleConstants.getForeground(style.attributes)
    }
    private fun LoggingConsole.getBackgroundColour(): Color
    {
        val style = doc.getCharacterElement(0)
        return StyleConstants.getBackground(style.attributes)
    }

}