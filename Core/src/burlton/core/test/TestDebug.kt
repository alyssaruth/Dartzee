package burlton.core.test

import burlton.core.code.util.Debug
import burlton.core.code.util.DebugOutput
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDebug
{
    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    @Before
    fun setup()
    {
        System.setOut(PrintStream(newOut))

        Debug.initialise(SimpleDebugOutput())
    }

    @After
    fun restore()
    {
        System.setOut(originalOut)
    }

    @Test
    fun testAppendSql()
    {
        Debug.clearLogs()

        val time = Debug.getCurrentTimeForLogging()
        val dayAndMonthStr = time.split(" ")[0]

        Debug.appendSql("SELECT * FROM Table", false)
        Debug.appendSql("SELECT * FROM OtherTable", true)

        Debug.waitUntilLoggingFinished()
        val logs = Debug.getLogs()

        assertThat(logs, containsSubstring("[SQL] SELECT * FROM OtherTable"))
        assertThat(logs, containsSubstring(dayAndMonthStr))
        assertFalse(logs.contains("SELECT * FROM Table"))
    }

    @Test
    fun testAppendWithoutDate()
    {
        Debug.clearLogs()

        val time = Debug.getCurrentTimeForLogging()
        val dayAndMonthStr = time.split(" ")[0]

        Debug.appendWithoutDate("Not here", false)
        Debug.appendWithoutDate("Present")
        Debug.appendTabbed("Tabbed")

        Debug.waitUntilLoggingFinished()
        val logs = Debug.getLogs()

        assertThat(logs, containsSubstring("                                      Present"))
        assertThat(logs, containsSubstring("                                      \tTabbed"))
        assertFalse(logs.contains(dayAndMonthStr))
        assertFalse(logs.contains("Not here"))
    }

    @Test
    fun testAppendBanner()
    {
        Debug.clearLogs()

        Debug.appendBanner("NoBanner", false)
        Debug.appendBanner("IMPORTANT")

        Debug.waitUntilLoggingFinished()
        val logs = Debug.getLogs()

        assertFalse(logs.contains("NoBanner"))
        assertTrue(logs.contains("*************"))
        assertTrue(logs.contains("* IMPORTANT *"))
        assertFalse(logs.contains("**************"))
    }

    @Test
    fun testLoggingToSystemOut()
    {
        Debug.clearLogs()

        Debug.setLogToSystemOut(false)
        Debug.append("NotOut")

        Debug.waitUntilLoggingFinished()

        Debug.setLogToSystemOut(true)
        Debug.append("ToOut")

        Debug.waitUntilLoggingFinished()

        val debugLogs = Debug.getLogs()
        val systemOutLogs = newOut.toString()

        assertTrue(debugLogs.contains("NotOut"))
        assertTrue(debugLogs.contains("ToOut"))
        assertFalse(systemOutLogs.contains("NotOut"))
        assertTrue(systemOutLogs.contains("ToOut"))
    }

    @Test
    fun testStackTraceBasic()
    {
        Debug.clearLogs()
        Debug.stackTrace("This is a test")

        Debug.waitUntilLoggingFinished()

        val logs = Debug.getLogs()

        assertTrue(logs.contains("This is a test"))
        assertTrue(logs.contains("java.lang.Throwable"))
        assertTrue(logs.contains("TestDebug.testStackTraceBasic(TestDebug.kt:"))
    }

    @Test
    fun testNewLine()
    {
        Debug.clearLogs()
        Debug.newLine()
        Debug.waitUntilLoggingFinished()

        val logs = Debug.getLogs()
        assertThat(logs, equalTo("\n                                      "))
    }


    class SimpleDebugOutput: DebugOutput
    {
        val sb = StringBuilder()

        override fun append(text: String?)
        {
            sb.append(text)
        }

        override fun getLogs(): String
        {
            return sb.toString()
        }

        override fun clear()
        {
            sb.setLength(0)
        }
    }
}