package dartzee.core.util

import dartzee.core.helper.exceptionLogged
import dartzee.core.helper.getLogs
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.CoreRegistry.INSTANCE_STRING_USER_NAME
import dartzee.core.util.CoreRegistry.instance
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.mockk.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TestDebug: AbstractTest()
{
    private val ext = Debug.debugExtension
    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    private val originalName = instance.get(INSTANCE_STRING_USER_NAME, "")

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        instance.put(INSTANCE_STRING_USER_NAME, "TestUser")

        System.setOut(PrintStream(newOut))
        Debug.lastEmailMillis = -1
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        System.setOut(originalOut)

        instance.put(INSTANCE_STRING_USER_NAME, originalName)

        Debug.debugExtension = ext
        Debug.sendingEmails = false
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

        Debug.logToSystemOut = false
        Debug.append("NotOut")

        Debug.waitUntilLoggingFinished()

        Debug.logToSystemOut = true
        Debug.append("ToOut")

        val debugLogs = getLogs()
        val systemOutLogs = newOut.toString()

        debugLogs shouldContain("NotOut")
        debugLogs shouldContain("ToOut")

        systemOutLogs shouldNotContain("NotOut")
        systemOutLogs shouldContain("ToOut")
    }

    /**
     * Stack traces
     */
    @Test
    fun testStackTraceBasic()
    {
        Debug.clearLogs()
        Debug.stackTrace(message = "This is a test", suppressError = true)

        val logs = getLogs()

        logs shouldContain("This is a test")
        logs shouldContain("java.lang.Throwable")
        logs shouldContain("TestDebug.testStackTraceBasic(TestDebug.kt:")

        exceptionLogged() shouldBe true
    }

    @Test
    fun `Should show an error and send an email for a regular stack trace`()
    {
        val ext = mockk<DebugExtension>(relaxed = true)
        Debug.debugExtension = ext
        Debug.sendingEmails = true

        Debug.stackTrace("Foo")

        exceptionLogged() shouldBe true
        verify { ext.exceptionCaught(true) }
        verify { ext.sendEmail("java.lang.Throwable - Foo () - TestUser", any()) }

        getLogs() shouldContain Debug.SUCCESS_MESSAGE
    }

    @Test
    fun `Should disable emailing if an error occurs trying to email a log`()
    {
        val ext = mockk<DebugExtension>(relaxed = true)
        Debug.debugExtension = ext
        Debug.sendingEmails = true

        every { ext.sendEmail(any(), any()) } throws Exception("Not again")
        Debug.stackTrace("Foo")

        exceptionLogged() shouldBe true
        verify { ext.exceptionCaught(true) }
        verify { ext.sendEmail("java.lang.Throwable - Foo () - TestUser", any()) }
        verify { ext.unableToEmailLogs() }

        getLogs() shouldNotContain Debug.SUCCESS_MESSAGE
        getLogs() shouldContain "Foo"
        getLogs() shouldContain "Not again"

        confirmVerified(ext)
        clearMocks(ext)

        Debug.stackTrace("Foo2")

        getLogs() shouldContain "Foo2"
        verifyNotCalled { ext.sendEmail(any(), any()) }
    }

    @Test
    fun testNewLine()
    {
        Debug.clearLogs()
        Debug.newLine()

        val logs = getLogs()
        logs shouldBe("\n                                      ")
    }

    class SimpleDebugOutput: DebugOutput
    {
        val sb = StringBuilder()

        override fun append(text: String)
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