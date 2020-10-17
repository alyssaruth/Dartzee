package dartzee.utils

import dartzee.helper.AbstractTest
import dartzee.helper.assertExits
import dartzee.helper.dropAllTables
import dartzee.logging.*
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsDatabaseUtil: AbstractTest()
{
    @Test
    fun `Should log a warning, show an error and exit if DB version is too old`()
    {
        assertExits(1) {
            DartsDatabaseUtil.initialiseDatabase(DartsDatabaseUtil.MIN_DB_VERSION_FOR_CONVERSION - 1)
        }

        dialogFactory.errorsShown.shouldHaveSize(1)
        verifyLog(CODE_DATABASE_TOO_OLD, Severity.WARN)
    }

    @Test
    fun `Should initialise a fresh database if no version is found`()
    {
        clearLogs()
        dropAllTables()

        DartsDatabaseUtil.initialiseDatabase()

        verifyLog(CODE_DATABASE_CREATING)
        verifyLog(CODE_DATABASE_CREATED)

        mainDatabase.getDatabaseVersion() shouldBe DATABASE_VERSION
    }

    @Test
    fun `Should do nothing if db already up to date`()
    {
        clearLogs()
        DartsDatabaseUtil.initialiseDatabase(DATABASE_VERSION)

        verifyLog(CODE_DATABASE_UP_TO_DATE)
        verifyNoLogs(CODE_SQL)
        verifyNoLogs(CODE_TABLE_CREATED)
    }
}