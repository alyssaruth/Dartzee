package dartzee.helper

import com.github.alyssaburlton.swingtest.SwingTestCleanupExtension
import dartzee.core.helper.TestMessageDialogFactory
import dartzee.core.util.DialogUtil
import dartzee.logging.LogDestinationSystemOut
import dartzee.logging.LogRecord
import dartzee.logging.Logger
import dartzee.logging.LoggingCode
import dartzee.logging.Severity
import dartzee.`object`.DartsClient
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.assertions.fail
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

private val logDestination = FakeLogDestination()
val logger = Logger(listOf(logDestination, LogDestinationSystemOut()))
private var checkedForExceptions = false

const val TEST_ROOT = "Test/"
const val TEST_DB_DIRECTORY = "Test/Databases"

@ExtendWith(BeforeAllTestsExtension::class)
@ExtendWith(SwingTestCleanupExtension::class)
open class AbstractTest {
    val dialogFactory = TestMessageDialogFactory()

    @BeforeEach
    fun beforeEachTest() {
        ScreenCache.emptyCache()
        dialogFactory.reset()
        clearLogs()
        clearAllMocks()

        DialogUtil.init(dialogFactory)
        DartsClient.devMode = false

        mainDatabase.localIdGenerator.clearCache()

        if (logDestination.haveRunInsert) {
            wipeDatabase()
            logDestination.haveRunInsert = false
        }

        InjectedThings.esDestination = mockk(relaxed = true)
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()
        InjectedThings.partyMode = false

        logger.loggingContext.clear()
    }

    @AfterEach
    fun afterEachTest() {
        if (!checkedForExceptions) {
            val errors = getErrorsLogged()
            if (errors.isNotEmpty()) {
                fail(
                    "Unexpected error(s) were logged during test: ${errors.map { it.toJsonString() } }"
                )
            }
            errorLogged() shouldBe false
        }

        checkedForExceptions = false
    }

    fun wipeDatabase() {
        DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach { wipeTable(it.getTableName()) }
    }

    fun getLastLog() = flushAndGetLogRecords().last()

    fun verifyLog(code: LoggingCode, severity: Severity = Severity.INFO): LogRecord {
        val record =
            flushAndGetLogRecords().findLast { it.loggingCode == code && it.severity == severity }
        record.shouldNotBeNull()

        if (severity == Severity.ERROR) {
            checkedForExceptions = true
        }

        return record
    }

    protected fun findLog(code: LoggingCode, severity: Severity = Severity.INFO) =
        getLogRecordsSoFar().findLast { it.loggingCode == code && it.severity == severity }

    fun verifyNoLogs(code: LoggingCode) {
        flushAndGetLogRecords().any { it.loggingCode == code } shouldBe false
    }

    fun errorLogged(): Boolean {
        checkedForExceptions = true
        return getErrorsLogged().isNotEmpty()
    }

    private fun getErrorsLogged() = flushAndGetLogRecords().filter { it.severity == Severity.ERROR }

    fun getLogRecordsSoFar() = logDestination.logRecords.toList()

    fun flushAndGetLogRecords(): List<LogRecord> {
        logger.waitUntilLoggingFinished()
        return logDestination.logRecords.toList()
    }

    fun clearLogs() {
        logger.waitUntilLoggingFinished()
        logDestination.clear()
    }
}
