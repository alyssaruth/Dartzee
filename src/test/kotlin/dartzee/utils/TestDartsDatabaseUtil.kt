package dartzee.utils

import dartzee.db.VersionEntity
import dartzee.helper.AbstractTest
import dartzee.helper.FakeTerminator
import dartzee.helper.TerminationException
import dartzee.logging.CODE_DATABASE_TOO_OLD
import dartzee.logging.Severity
import io.kotlintest.matchers.collections.shouldHaveSize
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
}