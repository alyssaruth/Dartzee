package dartzee.helper

import dartzee.core.helper.TestMessageDialogFactory
import dartzee.core.util.DialogUtil
import dartzee.logging.LogDestinationSystemOut
import dartzee.logging.LogRecord
import dartzee.logging.Logger
import dartzee.logging.LoggingCode
import dartzee.logging.Severity
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.Window
import javax.swing.SwingUtilities

private val logDestination = FakeLogDestination()
val logger = Logger(listOf(logDestination, LogDestinationSystemOut()))
private var checkedForExceptions = false

const val TEST_ROOT = "Test/"
const val TEST_DB_DIRECTORY = "Test/Databases"

@ExtendWith(BeforeAllTestsExtension::class)
abstract class AbstractTest
{
    val dialogFactory = TestMessageDialogFactory()

    @BeforeEach
    fun beforeEachTest()
    {
        ScreenCache.emptyCache()
        dialogFactory.reset()
        clearLogs()
        clearAllMocks()

        DialogUtil.init(dialogFactory)

        mainDatabase.localIdGenerator.hmLastAssignedIdByEntityName.clear()

        if (logDestination.haveRunInsert)
        {
            wipeDatabase()
            logDestination.haveRunInsert = false
        }

        InjectedThings.esDestination = mockk(relaxed = true)
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()

        logger.loggingContext.clear()
    }

    @AfterEach
    fun afterEachTest()
    {
        if (!checkedForExceptions)
        {
            errorLogged() shouldBe false
        }

        val visibleWindows = Window.getWindows().filter { it.isVisible }
        if (visibleWindows.isNotEmpty())
        {
            SwingUtilities.invokeLater { visibleWindows.forEach { it.dispose() } }
        }

        checkedForExceptions = false
    }

    fun wipeDatabase()
    {
        DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach { wipeTable(it.getTableName()) }
    }

    fun getLastLog() = getLogRecords().last()

    fun verifyLog(code: LoggingCode, severity: Severity = Severity.INFO): LogRecord
    {
        val record = getLogRecords().findLast { it.loggingCode == code && it.severity == severity }
        record.shouldNotBeNull()

        if (severity == Severity.ERROR)
        {
            checkedForExceptions = true
        }

        return record
    }

    fun verifyNoLogs(code: LoggingCode)
    {
        getLogRecords().any { it.loggingCode == code } shouldBe false
    }

    fun errorLogged(): Boolean
    {
        checkedForExceptions = true
        return getLogRecords().any { it.severity == Severity.ERROR }
    }

    fun getLogRecordsSoFar(): List<LogRecord>
    {
        return logDestination.logRecords.toList()
    }

    fun getLogRecords(): List<LogRecord>
    {
        logger.waitUntilLoggingFinished()
        return logDestination.logRecords.toList()
    }
    fun clearLogs()
    {
        logger.waitUntilLoggingFinished()
        logDestination.clear()
    }
}