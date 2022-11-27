package dartzee.sync

import com.github.alexburlton.swingtest.flushEdt
import dartzee.helper.*
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.logging.CODE_PULL_ERROR
import dartzee.logging.Severity
import dartzee.utils.Database
import dartzee.utils.InjectedThings.databaseDirectory
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException

class TestSyncManagerPull: AbstractTest()
{
    @BeforeEach
    fun beforeEach()
    {
        File(TEST_DB_DIRECTORY).mkdirs()
    }

    @AfterEach
    fun afterEach()
    {
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
        flushEdt()

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
    fun `Should abort the pull if some other validation error occurs with the database`()
    {
        usingInMemoryDatabase { db ->
            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to db)

            val manager = SyncManager(store)
            val t = manager.doPull(REMOTE_NAME)
            t.join()

            val log = verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
            log.message shouldBe "Unable to ascertain remote database version (but could connect) - this is unexpected."
            dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
        }
    }

    @Test
    fun `Should pull and swap in remote database`()
    {
        usingInMemoryDatabase(withSchema = true) { db ->
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
    fun `Should update sync screen regardless of an error occurring`()
    {
        shouldUpdateSyncScreen {
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