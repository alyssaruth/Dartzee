package dartzee.logging

import dartzee.CURRENT_TIME_STRING
import dartzee.helper.AbstractTest
import dartzee.makeLogRecord
import io.kotlintest.shouldBe
import org.junit.Test

class TestLogRecord: AbstractTest()
{
    @Test
    fun `Should render as a string for the logging console`()
    {
        val record = makeLogRecord(loggingCode = LoggingCode("some.code"),
            message = "This is a log")

        record.toString() shouldBe "$CURRENT_TIME_STRING   [some.code] This is a log"
    }

    @Test
    fun `Should return NULL if no error object`()
    {
        val record = makeLogRecord()
        record.getThrowableStr() shouldBe null
    }

    @Test
    fun `Should return the dated stack trace if it exists`()
    {
        val t = Throwable("Boom")
        val record = makeLogRecord(errorObject = t)

        record.getThrowableStr() shouldBe "$CURRENT_TIME_STRING   ${extractStackTrace(t)}"
    }
}