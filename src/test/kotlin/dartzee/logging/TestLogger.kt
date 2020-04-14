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
    private val destination = Logger.destinations

    override fun beforeEachTest()
    {
        Logger.destinations.clear()
    }

    override fun afterEachTest()
    {
        Logger.destinations.clear()
        Logger.destinations.addAll(destination)
    }

    @Test
    fun `Should log INFO`()
    {
        val destination = FakeLogDestination()
        Logger.destinations.add(destination)

        val loggingCode = LoggingCode("some.event")
        Logger.logInfo(loggingCode, "A thing happened")

        val record = destination.awaitLogs().first()
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
        Logger.destinations.add(destination)

        val loggingCode = LoggingCode("some.event")
        Logger.logInfo(loggingCode, "A thing happened", "Key" to "Value")

        val record = destination.awaitLogs().first()
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
        Logger.destinations.add(destination)

        val loggingCode = LoggingCode("bad.thing")
        val throwable = Throwable("Boo")
        Logger.logError(LoggingCode("bad.thing"), "An exception happened!", throwable, "other.info" to 60)

        val record = destination.awaitLogs().first()
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
        Logger.destinations.add(destination)

        val sql = "INSERT INTO Foo VALUES ('hello')"
        val genericSql = "INSERT INTO Foo VALUES (?)"

        Logger.logSql(sql, genericSql, 150)

        val record = destination.awaitLogs().first()
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
        Logger.destinations.add(destinationOne)
        Logger.destinations.add(destinationTwo)

        Logger.logInfo(LoggingCode("foo"), "bar")

        destinationOne.awaitLogs().shouldHaveSize(1)
        destinationTwo.awaitLogs().shouldHaveSize(1)
    }

    @Test
    fun `Should not log on the current thread, but should be possible to await all logging having finished`()
    {
        val destination = SleepyLogDestination()
        Logger.destinations.add(destination)

        Logger.logInfo(LoggingCode("foo"), "bar")

        destination.logRecords.shouldBeEmpty()
        Logger.waitUntilLoggingFinished()
        destination.logRecords.shouldHaveSize(1)
    }

    @Test
    fun `Should be possible to continue logging after awaiting logging to finish`()
    {
        val destination = SleepyLogDestination()
        Logger.destinations.add(destination)

        Logger.logInfo(LoggingCode("foo"), "bar")
        Logger.waitUntilLoggingFinished()

        Logger.logInfo(LoggingCode("foo"), "baz")
        Logger.waitUntilLoggingFinished()

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