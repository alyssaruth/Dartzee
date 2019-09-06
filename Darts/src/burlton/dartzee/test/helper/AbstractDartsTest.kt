package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.DartsClient
import burlton.dartzee.code.db.LocalIdGenerator
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.dartzee.code.utils.InjectedThings
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import org.apache.derby.jdbc.EmbeddedDriver
import java.sql.DriverManager
import javax.swing.UIManager

private const val DATABASE_NAME_TEST = "jdbc:derby:memory:Darts;create=true"
private var doneOneTimeSetup = false

abstract class AbstractDartsTest: AbstractDesktopTest()
{
    override fun doOneTimeDartsSetup()
    {
        if (doneOneTimeSetup)
        {
            return
        }

        //This is actually needed, annoyingly...
        DialogUtil.init(dialogFactory)

        InjectedThings.dartzeeCalculator = TestDartzeeCalculator()

        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        DartsClient.derbyDbName = DATABASE_NAME_TEST
        DriverManager.registerDriver(EmbeddedDriver())
        DartsDatabaseUtil.initialiseDatabase()

        doneOneTimeSetup = true
    }

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        LocalIdGenerator.hmLastAssignedIdByTableName.clear()

        DartsDatabaseUtil.getAllEntities().forEach { wipeTable(it.getTableName()) }
    }
}