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
}