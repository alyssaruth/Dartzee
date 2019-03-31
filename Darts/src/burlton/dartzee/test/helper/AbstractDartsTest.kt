package burlton.dartzee.test.helper

import burlton.core.code.util.AbstractClient
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import org.apache.derby.jdbc.EmbeddedDriver
import org.junit.Before
import java.sql.DriverManager

private const val DEBUG_MODE = true
private const val DATABASE_NAME_TEST = "jdbc:derby:memory:Darts;create=true"
private var doneOneTimeSetup = false

abstract class AbstractDartsTest: AbstractDesktopTest()
{
    private var doneClassSetup = false

    @Before
    fun oneTimeDartsSetup()
    {
        if (doneOneTimeSetup)
        {
            return
        }

        AbstractClient.derbyDbName = DATABASE_NAME_TEST
        DriverManager.registerDriver(EmbeddedDriver())
        DartsDatabaseUtil.initialiseDatabase()

        doneOneTimeSetup = true
    }
}