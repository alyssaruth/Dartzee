package dartzee.helper

import dartzee.CURRENT_TIME
import dartzee.core.helper.TestMessageDialogFactory
import dartzee.core.util.DialogUtil
import dartzee.db.LocalIdGenerator
import dartzee.logging.*
import dartzee.screen.Dartboard
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import io.kotlintest.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.apache.derby.jdbc.EmbeddedDriver
import org.junit.After
import org.junit.Before
import java.awt.Window
import java.sql.DriverManager
import java.time.Clock
import java.time.ZoneId
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.test.assertNotNull

const val DATABASE_NAME_TEST = "jdbc:derby:memory:Darts;create=true"
private var doneOneTimeSetup = false
private val logDestination = FakeLogDestination()
val logger = Logger(listOf(logDestination, LogDestinationSystemOut()))
private var checkedForExceptions = false

abstract class AbstractTest
{
    private var doneClassSetup = false
    protected val dialogFactory = TestMessageDialogFactory()

    @Before
    fun oneTimeSetup()
    {
        if (!doneOneTimeSetup)
        {
            doOneTimeSetup()
            doneOneTimeSetup = true
        }

        if (!doneClassSetup)
        {
            doClassSetup()
            doneClassSetup = true
        }

        beforeEachTest()
    }

    private fun doOneTimeSetup()
    {
        DialogUtil.init(dialogFactory)

        Thread.setDefaultUncaughtExceptionHandler(LoggerUncaughtExceptionHandler())

        InjectedThings.logger = logger
        InjectedThings.verificationDartboardSize = 50
        InjectedThings.clock = Clock.fixed(CURRENT_TIME, ZoneId.of("UTC"))

        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        InjectedThings.mainDatabase = Database(dbName = DATABASE_NAME_TEST)
        DriverManager.registerDriver(EmbeddedDriver())
        DartsDatabaseUtil.initialiseDatabase()
    }

    open fun doClassSetup()
    {
        DialogUtil.init(dialogFactory)
        InjectedThings.esDestination = mockk(relaxed = true)
    }

    open fun beforeEachTest()
    {
        ScreenCache.emptyCache()
        dialogFactory.reset()
        clearLogs()
        clearAllMocks()

        LocalIdGenerator.hmLastAssignedIdByTableName.clear()
        DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach { wipeTable(it.getTableName()) }
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()

        //Clear cached dartboards
        Dartboard.appearancePreferenceChanged()

        logger.loggingContext.clear()
    }

    @After
    open fun afterEachTest()
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