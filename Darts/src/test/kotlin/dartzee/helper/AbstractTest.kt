package dartzee.test.helper

import dartzee.`object`.DartsClient
import dartzee.core.util.Debug
import dartzee.core.util.DialogUtil
import dartzee.db.LocalIdGenerator
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings
import dartzee.test.core.helper.TestDebugExtension
import dartzee.test.core.helper.TestMessageDialogFactory
import dartzee.test.core.helper.checkedForExceptions
import dartzee.test.core.helper.exceptionLogged
import dartzee.test.core.util.TestDebug
import io.kotlintest.shouldBe
import org.apache.derby.jdbc.EmbeddedDriver
import org.junit.After
import org.junit.Before
import java.sql.DriverManager
import javax.swing.UIManager

private const val DATABASE_NAME_TEST = "jdbc:derby:memory:Darts;create=true"
private var doneOneTimeSetup = false

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
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.sendingEmails = false
        Debug.logToSystemOut = true

        Debug.debugExtension = TestDebugExtension()
        DialogUtil.init(dialogFactory)

        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()
        InjectedThings.verificationDartboardSize = 50

        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        DartsClient.derbyDbName = DATABASE_NAME_TEST
        DriverManager.registerDriver(EmbeddedDriver())
        DartsDatabaseUtil.initialiseDatabase()
    }

    open fun doClassSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.logToSystemOut = true
        DialogUtil.init(dialogFactory)
    }

    open fun beforeEachTest()
    {
        Debug.lastErrorMillis = -1
        Debug.initialise(TestDebug.SimpleDebugOutput())
        dialogFactory.reset()

        LocalIdGenerator.hmLastAssignedIdByTableName.clear()
        DartsDatabaseUtil.getAllEntities().forEach { wipeTable(it.getTableName()) }
    }

    @After
    open fun afterEachTest()
    {
        if (!checkedForExceptions)
        {
            exceptionLogged() shouldBe false
        }

        checkedForExceptions = false
    }
}