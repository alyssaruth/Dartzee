package burlton.core.test

import burlton.core.code.util.Debug
import burlton.core.code.util.DebugOutput
import burlton.core.test.helper.getLogs
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TestDebug
{
    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    @Before
    fun setup()
    {
        System.setOut(PrintStream(newOut))
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

        val logs = getLogs()

        logs shouldContain("[SQL] SELECT * FROM OtherTable")
        logs shouldContain(dayAndMonthStr)

        logs shouldNotContain("SELECT * FROM Table")
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

        val logs = getLogs()

        logs shouldContain("                                      Present")
        logs shouldContain("                                      \tTabbed")

        logs shouldNotContain(dayAndMonthStr)
        logs shouldNotContain("Not here")
    }

    @Test
    fun testAppendBanner()
    {
        Debug.clearLogs()

        Debug.appendBanner("NoBanner", false)
        Debug.appendBanner("IMPORTANT")

        val logs = getLogs()

        logs shouldNotContain("NoBanner")
        logs shouldContain("*************")
        logs shouldContain("* IMPORTANT *")
        logs shouldNotContain("**************")
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

        val debugLogs = getLogs()
        val systemOutLogs = newOut.toString()

        debugLogs shouldContain("NotOut")
        debugLogs shouldContain("ToOut")

        systemOutLogs shouldNotContain("NotOut")
        systemOutLogs shouldContain("ToOut")
    }

    @Test
    fun testStackTraceBasic()
    {
        Debug.clearLogs()
        Debug.stackTraceNoError("This is a test")

        val logs = getLogs()

        logs shouldContain("This is a test")
        logs shouldContain("java.lang.Throwable")
        logs shouldContain("TestDebug.testStackTraceBasic(TestDebug.kt:")
    }

    @Test
    fun testNewLine()
    {
        Debug.clearLogs()
        Debug.newLine()

        val logs = getLogs()
        logs shouldBe("\n                                      ")
    }

    /*@Test
    fun testNonBlocking()
    {
        whenInvoke(Debug.appendInCurrentThread(anyString(), anyBoolean(), null)).then{
            Thread.sleep(10000)
        }

        val currentMillis = System.currentTimeMillis()
        Debug.append("longTest")

        val afterMillis = System.currentTimeMillis()
        val diff = afterMillis - currentMillis
        assertThat(diff, lessThan(1000))
    }*/

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