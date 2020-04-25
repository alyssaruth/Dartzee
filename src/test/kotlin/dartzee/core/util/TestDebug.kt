package dartzee.core.util

import dartzee.core.helper.exceptionLogged
import dartzee.core.helper.getLogs
import dartzee.helper.AbstractTestWithUsername
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TestDebug: AbstractTestWithUsername()
{
    private val ext = Debug.debugExtension
    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        System.setOut(PrintStream(newOut))
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        System.setOut(originalOut)

        Debug.debugExtension = ext
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