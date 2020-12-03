package dartzee.sync

import dartzee.helper.*
import dartzee.logging.CODE_PULL_ERROR
import dartzee.logging.Severity
import dartzee.utils.Database
import dartzee.utils.InjectedThings.databaseDirectory
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.File
import java.io.IOException

class TestSyncManagerPull: AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()
        File(TEST_DB_DIRECTORY).mkdirs()
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        File(TEST_DB_DIRECTORY).deleteRecursively()
    }

    @Test
    fun `Should log an error and dismiss the loading dialog if an error occurs`()
    {
        val exception = IOException("Boom.")
        val dbStore = mockk<IRemoteDatabaseStore>()
        every { dbStore.fetchDatabase(any()) } throws exception

        val manager = SyncManager(dbStore)
        val t = manager.doPull(REMOTE_NAME)
        t.join()

        dialogFactory.loadingsShown.shouldContainExactly("Pulling $REMOTE_NAME...")
        dialogFactory.loadingVisible shouldBe false

        val log = verifyLog(CODE_PULL_ERROR, Severity.ERROR)
        log.errorObject shouldBe exception

        dialogFactory.errorsShown.shouldContainExactly("An unexpected error occurred - no data has been changed.")

        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should abort the pull if test connection cannot be established`()
    {
        val db = mockk<Database>(relaxed = true)
        every { db.testConnection() } returns false

        val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to db)

        val manager = SyncManager(store)
        val t = manager.doPull(REMOTE_NAME)
        t.join()

        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
    }

    @Test
    fun `Should pull and swap in remote database`()
    {
        usingInMemoryDatabase { db ->
            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to db)

            val f = File("${db.getDirectoryStr()}/SomeFile.txt")
            f.createNewFile()

            val manager = SyncManager(store)
            val t = manager.doPull(REMOTE_NAME)
            t.join()

            File("$databaseDirectory/Darts/SomeFile.txt").shouldExist()
        }
    }

    @Test
    fun `Should update sync summary regardless of an error occurring`()
    {
        shouldUpdateSyncSummary {
            val exception = IOException("Boom.")
            val dbStore = mockk<IRemoteDatabaseStore>()
            every { dbStore.fetchDatabase(any()) } throws exception

            val manager = SyncManager(dbStore)
            val t = manager.doPull(REMOTE_NAME)
            t.join()

            errorLogged() shouldBe true
        }
    }
}