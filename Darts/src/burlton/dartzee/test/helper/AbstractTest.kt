package burlton.dartzee.test.helper

import burlton.core.code.util.AbstractClient
import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.test.helpers.TestMessageDialogFactory
import org.apache.derby.jdbc.EmbeddedDriver
import org.junit.Before
import java.sql.DriverManager


private const val DATABASE_NAME_TEST = "jdbc:derby:memory:Darts;create=true"

abstract class AbstractTest
{
    private var doneOneTimeSetup = false
    private var doneClassSetup = false
    protected val dialogFactory = TestMessageDialogFactory()

    @Before
    fun beforeAllTests()
    {
        if (doneOneTimeSetup)
        {
            return
        }

        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.setLogToSystemOut(false)
        DialogUtil.init(dialogFactory)

        AbstractClient.derbyDbName = DATABASE_NAME_TEST
        DriverManager.registerDriver(EmbeddedDriver())
        DartsDatabaseUtil.initialiseDatabase()
    }

    @Before
    fun beforeClassGeneric()
    {
        if (doneClassSetup)
        {
            return
        }

        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.setLogToSystemOut(false)
        DialogUtil.init(dialogFactory)

        beforeClass()

        doneClassSetup = true
    }

    @Before
    open fun beforeClass() {}

    @Before
    open fun beforeEachTest()
    {
        dialogFactory.reset()
    }
}