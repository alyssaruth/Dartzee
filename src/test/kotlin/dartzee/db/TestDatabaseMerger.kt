package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.makeInMemoryDatabase
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.logging.CODE_TEST_CONNECTION_ERROR
import dartzee.logging.Severity
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class TestDatabaseMerger: AbstractTest()
{
    @Test
    fun `Should return false if connecting to remote database fails`()
    {
        val localDatabase = makeInMemoryDatabase()

        val remoteName = "jdbc:derby:memory:invalid;create=false"
        val remoteDatabase = Database("invalid", remoteName)

        val merger = DatabaseMerger(localDatabase, remoteDatabase)
        merger.validateMerge() shouldBe false
        verifyLog(CODE_TEST_CONNECTION_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
    }

    @Test
    fun `Should return false and log an error if remote database version cannot be verified`()
    {
        val remoteDatabase = mockk<Database>(relaxed = true)
        every { remoteDatabase.testConnection() } returns true
        every { remoteDatabase.getDatabaseVersion() } returns null
        
        val merger = DatabaseMerger(mainDatabase, remoteDatabase)
        merger.validateMerge() shouldBe false
        verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
    }

    @Test
    fun `Should return false if remote database has higher version`()
    {
        val remoteDatabase = makeInMemoryDatabase()
        remoteDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION + 1)

        val merger = DatabaseMerger(mainDatabase, remoteDatabase)
        merger.validateMerge() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The remote database contains data written by a higher Dartzee version. \n\nYou will need to update to the latest version of Dartzee before syncing again.")
    }
}