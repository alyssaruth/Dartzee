package dartzee.sync

import dartzee.core.util.getSqlDateNow
import dartzee.db.DeletionAuditEntity
import dartzee.db.EntityName
import dartzee.db.SyncAuditEntity
import dartzee.helper.AbstractTest
import dartzee.helper.LOG_DUMP_FILE
import dartzee.helper.REMOTE_NAME
import dartzee.helper.TEST_DB_DIRECTORY
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.shouldUpdateSyncScreen
import dartzee.helper.syncDirectoryShouldNotExist
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_FILE_ERROR
import dartzee.logging.CODE_REVERT_TO_PULL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.CODE_SYNC_ERROR
import dartzee.logging.KEY_GAME_IDS
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.github.alyssaruth.swingtest.waitForAssertion
import io.github.alyssaruth.swingtest.waitForErrorDialog
import io.github.alyssaruth.swingtest.waitForInfoDialog
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.sql.Timestamp
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncManagerTest : AbstractTest() {
    @BeforeEach
    fun beforeEach() {
        File(TEST_DB_DIRECTORY).mkdirs()
    }

    @AfterEach
    fun afterEach() {
        File(TEST_DB_DIRECTORY).deleteRecursively()
        LOG_DUMP_FILE.delete()
    }

    @Test
    fun `Should revert to a PULL if no local changes`() {
        val syncManager = spyk(SyncManager(mockk(relaxed = true)))
        every { syncManager.doPull(any()) } returns Thread()

        val t = syncManager.doSyncIfNecessary(REMOTE_NAME)
        t.join()

        verify { syncManager.doPull(REMOTE_NAME) }
        verifyLog(CODE_REVERT_TO_PULL, Severity.INFO)
    }

    @Test
    fun `Should process with a sync if there are local changes`() {
        insertGame()

        val syncManager = spyk(SyncManager(mockk(relaxed = true)))
        every { syncManager.doSync(any()) } returns Thread()

        val t = syncManager.doSyncIfNecessary(REMOTE_NAME)
        t.join()

        waitForAssertion { verify { syncManager.doSync(REMOTE_NAME) } }
    }

    @Test
    fun `Should show an error and log a warning if there is a connection error`() {
        val exception = SocketException("Failed to connect.")
        val store = mockk<IRemoteDatabaseStore>()
        every { store.fetchDatabase(any()) } throws exception

        var t: Thread? = null
        runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }

        waitForErrorDialog(
            "A connection error occurred. Check your internet connection and try again."
        )

        waitForAssertion {
            val log = verifyLog(CODE_SYNC_ERROR, Severity.WARN)
            log.message shouldBe "Caught network error during sync: $exception"
            syncDirectoryShouldNotExist()
        }

        t!!.join()
    }

    @Test
    fun `Should abort sync if merger validation fails`() {
        val remoteDb = mockk<Database>()
        every { remoteDb.testConnection() } returns false

        val store = InMemoryRemoteDatabaseStore("Goomba" to remoteDb)

        var t: Thread? = null
        runAsync { t = SyncManager(store).doSync("Goomba") }

        waitForErrorDialog("An error occurred connecting to the remote database.")

        waitForAssertion { syncDirectoryShouldNotExist() }
        t!!.join()
    }

    @Test
    fun `Should abort if a SQL error occurs during database merge`() {
        usingDbWithTestFile { remoteDb ->
            insertPlayer(database = mainDatabase)
            remoteDb.dropTable(EntityName.Player)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)

            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }

            waitForErrorDialog("An unexpected error occurred - no data has been changed.")

            waitForAssertion {
                val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
                log.message shouldContain "Caught SQLException for statement"
                log.errorObject?.message shouldContain "Table/View 'PLAYER' does not exist"

                syncDirectoryShouldNotExist()
                databasesSwapped() shouldBe false
            }

            t!!.join()
        }
    }

    @Test
    fun `Should throw an error if games from the original database have not made it through the merge`() {
        usingDbWithTestFile { remoteDb ->
            val g = insertGame(dtLastUpdate = Timestamp(1000), database = mainDatabase)
            SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)

            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }

            waitForErrorDialog("Sync resulted in missing data. \nResults have been discarded.")

            waitForAssertion {
                val log = verifyLog(CODE_SYNC_ERROR, Severity.ERROR)
                log.message shouldContain "1 game(s) missing from resulting database after merge"
                log.keyValuePairs[KEY_GAME_IDS] shouldBe setOf(g.rowId)

                syncDirectoryShouldNotExist()
                databasesSwapped() shouldBe false
            }

            t!!.join()
        }
    }

    @Test
    fun `Should not throw an error if game from original database was explicitly deleted on another device`() {
        usingDbWithTestFile { remoteDb ->
            val g = insertGame(dtLastUpdate = Timestamp(1000), database = mainDatabase)
            SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
            DeletionAuditEntity.factoryAndSave(g, remoteDb)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)

            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }
            waitForInfoDialog("Sync completed successfully!\nGames pushed: 0\nGames pulled: 0")

            waitForAssertion {
                syncDirectoryShouldNotExist()
                databasesSwapped() shouldBe true
            }

            t!!.join()
        }
    }

    @Test
    fun `Should show an error and log a warning if remote db has been modified since it was pulled`() {
        usingDbWithTestFile { remoteDb ->
            val exception = ConcurrentModificationException("Oh no")
            val store = mockk<IRemoteDatabaseStore>()
            every { store.fetchDatabase(any()) } returns
                FetchDatabaseResult(remoteDb, getSqlDateNow())
            every { store.pushDatabase(any(), any(), any()) } throws exception

            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }

            waitForErrorDialog(
                "Another sync has been performed since this one started. \nResults have been discarded."
            )

            waitForAssertion {
                val log = verifyLog(CODE_SYNC_ERROR, Severity.WARN)
                log.message shouldBe "$exception"

                syncDirectoryShouldNotExist()
                databasesSwapped() shouldBe false
            }

            t!!.join()
        }
    }

    @Test
    fun `Should show an error if something goes wrong swapping the database in, and dump logs`() {
        usingDbWithTestFile { remoteDb ->
            remoteDb.getDirectory().deleteRecursively()

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)
            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }

            waitForErrorDialog(
                "Failed to restore database. Error: Failed to rename new file to ${mainDatabase.dbName}"
            )

            verifyLog(CODE_FILE_ERROR, Severity.ERROR)

            // Open a test connection so the tidy-up doesn't freak out about the db already being
            // shut down
            t!!.join()
            remoteDb.testConnection()

            LOG_DUMP_FILE.shouldExist()
        }
    }

    @Test
    fun `Should update sync screen regardless of an error occurring`() {
        shouldUpdateSyncScreen {
            val exception = IOException("Boom.")
            val dbStore = mockk<IRemoteDatabaseStore>()
            every { dbStore.fetchDatabase(any()) } throws exception

            val manager = SyncManager(dbStore)
            runAsync { manager.doSync(REMOTE_NAME) }
            waitForErrorDialog("An unexpected error occurred - no data has been changed.")

            errorLogged() shouldBe true
        }
    }

    @Test
    fun `Should successfully sync data between remote and local db, pushing up the result and swapping in locally`() {
        usingDbWithTestFile { remoteDb ->
            insertGame(database = mainDatabase)
            insertGame(database = remoteDb)

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)
            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }

            val summary = "\nGames pushed: 1\nGames pulled: 1"
            val expectedInfoText = "Sync completed successfully!$summary"
            waitForInfoDialog(expectedInfoText)

            waitForAssertion {
                val resultingRemote = store.fetchDatabase(REMOTE_NAME).database
                getCountFromTable("Game", resultingRemote) shouldBe 2

                syncDirectoryShouldNotExist()
                databasesSwapped() shouldBe true
            }

            t!!.join()
        }
    }

    private fun usingDbWithTestFile(testBlock: (inMemoryDatabase: Database) -> Unit) {
        usingInMemoryDatabase(withSchema = true) { remoteDb ->
            val f = File("${remoteDb.getDirectoryStr()}/SomeFile.txt")
            f.createNewFile()

            testBlock(remoteDb)
        }
    }

    private fun databasesSwapped() =
        File("${InjectedThings.databaseDirectory}/Darts/SomeFile.txt").exists()
}
