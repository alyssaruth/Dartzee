package dartzee.helper

import dartzee.core.helper.TestMessageDialogFactory
import dartzee.core.util.DialogUtil
import dartzee.logging.*
import dartzee.screen.Dartboard
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.Window
import javax.swing.SwingUtilities
import kotlin.test.assertNotNull

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

        mainDatabase.localIdGenerator.hmLastAssignedIdByTableName.clear()

        if (logDestination.haveRunInsert)
        {
            DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach { wipeTable(it.getTableName()) }
            logDestination.haveRunInsert = false
        }

        InjectedThings.esDestination = mockk(relaxed = true)
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()

        //Clear cached dartboards
        Dartboard.appearancePreferenceChanged()

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

    fun getLastLog() = getLogRecords().last()

    fun verifyLog(code: LoggingCode, severity: Severity = Severity.INFO): LogRecord
    {
        val record = getLogRecords().findLast { it.loggingCode == code && it.severity == severity }
        assertNotNull(record)

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