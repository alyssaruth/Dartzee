package dartzee.sync

import dartzee.core.util.getSqlDateNow
import dartzee.db.EntityName
import dartzee.db.SyncAuditEntity
import dartzee.helper.*
import dartzee.logging.*
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.sql.Timestamp

class TestSyncManager: AbstractTest()
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
    fun `Should revert to a PULL if no local changes`()
    {
        val syncManager = spyk(SyncManager(mockk(relaxed = true)))
        val t = syncManager.doSyncIfNecessary(REMOTE_NAME)
        t.join()

        verify { syncManager.doPull(REMOTE_NAME) }
        verifyLog(CODE_REVERT_TO_PULL, Severity.INFO)
    }

    @Test
    fun `Should process with a sync if there are local changes`()
    {
        insertGame()

        val syncManager = spyk(SyncManager(mockk(relaxed = true)))
        val t = syncManager.doSyncIfNecessary(REMOTE_NAME)
        t.join()

        verify { syncManager.doSync(REMOTE_NAME) }
    }

    @Test
    fun `Should show an error and log a warning if there is a connection error`()
    {
        val exception = SocketException("Failed to connect.")
        val store = mockk<IRemoteDatabaseStore>()
        every { store.fetchDatabase(any()) } throws exception

        val t = SyncManager(store).doSync(REMOTE_NAME)
        t.join()

        dialogFactory.errorsShown.shouldContainExactly("A connection error occurred. Check your internet connection and try again.")
        val log = verifyLog(CODE_SYNC_ERROR, Severity.WARN)
        log.message shouldBe "Caught network error during sync: $exception"
        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should abort sync if merger validation fails`()
    {
        val remoteDb = mockk<Database>()
        every { remoteDb.testConnection() } returns false

        val store = InMemoryRemoteDatabaseStore("Goomba" to remoteDb)

        val t = SyncManager(store).doSync("Goomba")
        t.join()

        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should abort if a SQL error occurs during database merge`()
    {
        usingDbWithTestFile { remoteDb ->
            insertPlayer(database = mainDatabase)
            remoteDb.dropTable(EntityName.Player)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)

            val t = SyncManager(store).doSync(REMOTE_NAME)
            t.join()

            dialogFactory.errorsShown.shouldContainExactly("An unexpected error occurred - no data has been changed.")

            val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
            log.message shouldContain "Caught SQLException for statement"
            log.errorObject?.message shouldContain "Table/View 'PLAYER' does not exist"

            dialogFactory.infosShown.shouldBeEmpty()
            syncDirectoryShouldNotExist()
            databasesSwapped() shouldBe false
        }
    }

    @Test
    fun `Should throw an error if games from the original database have not made it through the merge`()
    {
        usingDbWithTestFile { remoteDb ->
            val g = insertGame(dtLastUpdate = Timestamp(1000), database = mainDatabase)
            SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)

            val t = SyncManager(store).doSync(REMOTE_NAME)
            t.join()

            dialogFactory.errorsShown.shouldContainExactly("Sync resulted in missing data. \n\nResults have been discarded.")

            val log = verifyLog(CODE_SYNC_ERROR, Severity.ERROR)
            log.message shouldContain "1 game(s) missing from resulting database after merge"
            log.keyValuePairs[KEY_GAME_IDS] shouldBe setOf(g.rowId)

            dialogFactory.infosShown.shouldBeEmpty()
            syncDirectoryShouldNotExist()
            databasesSwapped() shouldBe false
        }
    }

    @Test
    fun `Should show an error and log a warning if remote db has been modified since it was pulled`()
    {
        usingDbWithTestFile { remoteDb ->
            val exception = ConcurrentModificationException("Oh no")
            val store = mockk<IRemoteDatabaseStore>()
            every { store.fetchDatabase(any()) } returns FetchDatabaseResult(remoteDb, getSqlDateNow())
            every { store.pushDatabase(any(), any(), any()) } throws exception

            val t = SyncManager(store).doSync(REMOTE_NAME)
            t.join()

            dialogFactory.errorsShown.shouldContainExactly("Another sync has been performed since this one started. \n" +
                    "\n" +
                    "Results have been discarded.")

            val log = verifyLog(CODE_SYNC_ERROR, Severity.WARN)
            log.message shouldBe "$exception"

            dialogFactory.infosShown.shouldBeEmpty()
            syncDirectoryShouldNotExist()
            databasesSwapped() shouldBe false
        }
    }

    @Test
    fun `Should show an error if something goes wrong swapping the database in`()
    {
        usingDbWithTestFile { remoteDb ->
            remoteDb.getDirectory().deleteRecursively()

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)
            val t = SyncManager(store).doSync(REMOTE_NAME)
            t.join()

            dialogFactory.errorsShown.shouldContainExactly("Failed to restore database. Error: Failed to rename new file to ${mainDatabase.dbName}")
            dialogFactory.infosShown.shouldBeEmpty()

            // Open a test connection so the tidy-up doesn't freak out about the db already being shut down
            remoteDb.testConnection()
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
            val t = manager.doSync(REMOTE_NAME)
            t.join()

            errorLogged() shouldBe true
        }
    }

    @Test
    fun `Should successfully sync data between remote and local db, pushing up the result and swapping in locally`()
    {
        usingDbWithTestFile { remoteDb ->
            insertGame(database = mainDatabase)
            insertGame(database = remoteDb)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)
            val t = SyncManager(store).doSync(REMOTE_NAME)
            t.join()

            val resultingRemote = store.fetchDatabase(REMOTE_NAME).database
            getCountFromTable("Game", resultingRemote) shouldBe 2

            val summary = "\n\nGames pushed: 1\nGames pulled: 1"
            val expectedInfoText = "Sync completed successfully!$summary"
            dialogFactory.infosShown.shouldContainExactly(expectedInfoText)

            syncDirectoryShouldNotExist()
            databasesSwapped() shouldBe true
        }
    }

    private fun usingDbWithTestFile(testBlock: (inMemoryDatabase: Database) -> Unit)
    {
        usingInMemoryDatabase(withSchema = true) { remoteDb ->
            val f = File("${remoteDb.getDirectoryStr()}/SomeFile.txt")
            f.createNewFile()

            testBlock(remoteDb)
        }
    }

    private fun databasesSwapped() = File("${InjectedThings.databaseDirectory}/Darts/SomeFile.txt").exists()
}