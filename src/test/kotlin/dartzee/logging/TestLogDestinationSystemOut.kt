package dartzee.logging

import dartzee.CURRENT_TIME_STRING
import dartzee.helper.AbstractTest
import dartzee.makeLogRecord
import io.kotlintest.matchers.string.shouldContain
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class TestLogDestinationSystemOut: AbstractTest()
{
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
    }

    @Test
    fun `Should log the record to system out`()
    {
        val dest = LogDestinationSystemOut()

        val record = makeLogRecord(severity = Severity.INFO, loggingCode = LoggingCode("some.event"), message = "blah")
        dest.log(record)

        val output = newOut.toString()
        output shouldContain "$CURRENT_TIME_STRING   [some.event] blah"
    }

    @Test
    fun `Should print the stack trace for errors`()
    {
        val dest = LogDestinationSystemOut()

        val error = Throwable("oh no")
        val record = makeLogRecord(severity = Severity.ERROR, loggingCode = LoggingCode("some.event"), message = "blah", errorObject = error)
        dest.log(record)

        val output = newOut.toString()
        output shouldContain "$CURRENT_TIME_STRING   [some.event] blah"
        output shouldContain "$CURRENT_TIME_STRING   java.lang.Throwable: oh no"
    }
}