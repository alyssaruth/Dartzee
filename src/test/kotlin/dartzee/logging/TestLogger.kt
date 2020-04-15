package dartzee.logging

import dartzee.CURRENT_TIME
import dartzee.helper.AbstractTest
import dartzee.helper.FakeLogDestination
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestLogger: AbstractTest()
{
    @Test
    fun `Should log INFO`()
    {
        val destination = FakeLogDestination()
        val logger = Logger(listOf(destination))

        val loggingCode = LoggingCode("some.event")
        logger.logInfo(loggingCode, "A thing happened")
        logger.waitUntilLoggingFinished()

        val record = destination.logRecords.first()
        record.severity shouldBe Severity.INFO
        record.loggingCode shouldBe loggingCode
        record.message shouldBe "A thing happened"
        record.errorObject shouldBe null
        record.timestamp shouldBe CURRENT_TIME
        record.keyValuePairs.size shouldBe 0
    }

    @Test
    fun `Should support extra key values when logging INFO`()
    {
        val destination = FakeLogDestination()
        val logger = Logger(listOf(destination))

        val loggingCode = LoggingCode("some.event")
        logger.logInfo(loggingCode, "A thing happened", "Key" to "Value")
        logger.waitUntilLoggingFinished()

        val record = destination.logRecords.first()
        record.severity shouldBe Severity.INFO
        record.loggingCode shouldBe loggingCode
        record.message shouldBe "A thing happened"
        record.errorObject shouldBe null
        record.timestamp shouldBe CURRENT_TIME
        record.shouldContainKeyValues("Key" to "Value")
    }

    @Test
    fun `Should log ERROR`()
    {
        val destination = FakeLogDestination()
        val logger = Logger(listOf(destination))

        val loggingCode = LoggingCode("bad.thing")
        val throwable = Throwable("Boo")
        logger.logError(LoggingCode("bad.thing"), "An exception happened!", throwable, "other.info" to 60)
        logger.waitUntilLoggingFinished()

        val record = destination.logRecords.first()
        record.severity shouldBe Severity.ERROR
        record.errorObject shouldBe throwable
        record.loggingCode shouldBe loggingCode
        record.timestamp shouldBe CURRENT_TIME
        record.shouldContainKeyValues("other.info" to 60, KEY_EXCEPTION_MESSAGE to "Boo")
    }

    @Test
    fun `Should log SQL statements`()
    {
        val destination = FakeLogDestination()
        val logger = Logger(listOf(destination))

        val sql = "INSERT INTO Foo VALUES ('hello')"
        val genericSql = "INSERT INTO Foo VALUES (?)"

        logger.logSql(sql, genericSql, 150)
        logger.waitUntilLoggingFinished()

        val record = destination.logRecords.first()
        record.severity shouldBe Severity.INFO
        record.loggingCode shouldBe CODE_SQL
        record.message shouldBe "(150ms) $sql"
        record.errorObject shouldBe null
        record.timestamp shouldBe CURRENT_TIME
        record.shouldContainKeyValues(KEY_DURATION to 150L,
            KEY_GENERIC_SQL to genericSql,
            KEY_SQL to sql)
    }

    @Test
    fun `Should log to all destinations`()
    {
        val destinationOne = FakeLogDestination()
        val destinationTwo = FakeLogDestination()
        val logger = Logger(listOf(destinationOne, destinationTwo))
        logger.logInfo(LoggingCode("foo"), "bar")
        logger.waitUntilLoggingFinished()

        destinationOne.logRecords.shouldHaveSize(1)
        destinationTwo.logRecords.shouldHaveSize(1)
    }

    @Test
    fun `Should not log on the current thread, but should be possible to await all logging having finished`()
    {
        val destination = SleepyLogDestination()
        val logger = Logger(listOf(destination))

        logger.logInfo(LoggingCode("foo"), "bar")

        destination.logRecords.shouldBeEmpty()
        logger.waitUntilLoggingFinished()
        destination.logRecords.shouldHaveSize(1)
    }

    @Test
    fun `Should be possible to continue logging after awaiting logging to finish`()
    {
        val destination = SleepyLogDestination()
        val logger = Logger(listOf(destination))

        logger.logInfo(LoggingCode("foo"), "bar")
        logger.waitUntilLoggingFinished()

        logger.logInfo(LoggingCode("foo"), "baz")
        logger.waitUntilLoggingFinished()

        destination.logRecords.shouldHaveSize(2)
    }

    private fun LogRecord.shouldContainKeyValues(vararg values: Pair<String, Any?>)
    {
        keyValuePairs.shouldContainExactly(mapOf(*values))
    }
}

class SleepyLogDestination: ILogDestination
{
    val logRecords: MutableList<LogRecord> = mutableListOf()

    override fun log(record: LogRecord)
    {
        Thread.sleep(500)
        logRecords.add(record)
    }

    fun clear()
    {
        logRecords.clear()
    }
}