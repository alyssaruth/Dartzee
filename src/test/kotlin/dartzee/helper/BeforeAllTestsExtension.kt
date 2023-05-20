package dartzee.helper

import dartzee.CURRENT_TIME
import dartzee.core.helper.TestMessageDialogFactory
import dartzee.core.util.DialogUtil
import dartzee.logging.LoggerUncaughtExceptionHandler
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.time.Clock
import java.time.ZoneId
import javax.swing.UIManager

var doneOneTimeSetup = false

class BeforeAllTestsExtension: BeforeAllCallback
{
    override fun beforeAll(context: ExtensionContext?)
    {
        if (!doneOneTimeSetup)
        {
            doOneTimeSetup()
            doneOneTimeSetup = true
        }
    }

    private fun doOneTimeSetup()
    {
        DialogUtil.init(TestMessageDialogFactory())
        Thread.setDefaultUncaughtExceptionHandler(LoggerUncaughtExceptionHandler())

        InjectedThings.databaseDirectory = TEST_DB_DIRECTORY
        InjectedThings.logger = logger
        InjectedThings.clock = Clock.fixed(CURRENT_TIME, ZoneId.of("UTC"))
        InjectedThings.connectionPoolSize = 1

        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        InjectedThings.mainDatabase = Database(inMemory = true)
        DartsDatabaseUtil.initialiseDatabase(InjectedThings.mainDatabase)
    }
}