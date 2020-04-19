package dartzee.utils

import dartzee.db.VersionEntity
import dartzee.helper.AbstractTest
import dartzee.helper.FakeTerminator
import dartzee.helper.TerminationException
import dartzee.helper.dropAllTables
import dartzee.logging.*
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class TestDartsDatabaseUtil: AbstractTest()
{
    @Test
    fun `Should log a warning, show an error and exit if DB version is too old`()
    {
        InjectedThings.terminator = FakeTerminator()

        val version = VersionEntity()
        version.version = DartsDatabaseUtil.MIN_DB_VERSION_FOR_CONVERSION - 1

        shouldThrow<TerminationException> {
            DartsDatabaseUtil.initialiseDatabase(version)
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

        val version = VersionEntity.retrieveCurrentDatabaseVersion()
        version!!.version shouldBe DATABASE_VERSION
    }

    @Test
    fun `Should do nothing if db already up to date`()
    {
        val version = VersionEntity.retrieveCurrentDatabaseVersion()!!
        version.version shouldBe DATABASE_VERSION

        clearLogs()
        DartsDatabaseUtil.initialiseDatabase(version)

        verifyLog(CODE_DATABASE_UP_TO_DATE)
        verifyNoLogs(CODE_SQL)
        verifyNoLogs(CODE_TABLE_CREATED)
    }
}