package dartzee.sync

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.core.util.getSqlDateNow
import dartzee.db.DeletionAuditEntity
import dartzee.db.EntityName
import dartzee.db.SyncAuditEntity
import dartzee.findErrorDialog
import dartzee.findInfoDialog
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.getInfoDialog
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.helper.TEST_DB_DIRECTORY
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.shouldUpdateSyncScreen
import dartzee.helper.syncDirectoryShouldNotExist
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_REVERT_TO_PULL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.CODE_SYNC_ERROR
import dartzee.logging.KEY_GAME_IDS
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

class TestSyncManager : AbstractTest() {
    @BeforeEach
    fun beforeEach() {
        File(TEST_DB_DIRECTORY).mkdirs()
    }

    @AfterEach
    fun afterEach() {
        File(TEST_DB_DIRECTORY).deleteRecursively()
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

        waitForAssertion { findErrorDialog() shouldNotBe null }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe
            "A connection error occurred. Check your internet connection and try again."
        error.clickOk(async = true)

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
        waitForAssertion { findErrorDialog() shouldNotBe null }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "An error occurred connecting to the remote database."
        error.clickOk(async = true)

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
            waitForAssertion { findErrorDialog() shouldNotBe null }

            val error = getErrorDialog()
            error.getDialogMessage() shouldBe
                "An unexpected error occurred - no data has been changed."
            error.clickOk(async = true)

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
            waitForAssertion { findErrorDialog() shouldNotBe null }

            val error = getErrorDialog()
            error.getDialogMessage() shouldBe
                "Sync resulted in missing data. \n\nResults have been discarded."
            error.clickOk(async = true)

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
            waitForAssertion { findInfoDialog() shouldNotBe null }
            getInfoDialog().clickOk(async = true)

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
            waitForAssertion { findErrorDialog() shouldNotBe null }

            val error = getErrorDialog()
            error.getDialogMessage() shouldBe
                "Another sync has been performed since this one started. \n" +
                    "\n" +
                    "Results have been discarded."
            error.clickOk(async = true)

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
    fun `Should show an error if something goes wrong swapping the database in`() {
        usingDbWithTestFile { remoteDb ->
            remoteDb.getDirectory().deleteRecursively()

            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to remoteDb)
            var t: Thread? = null
            runAsync { t = SyncManager(store).doSync(REMOTE_NAME) }
            waitForAssertion { findErrorDialog() shouldNotBe null }

            val error = getErrorDialog()
            error.getDialogMessage() shouldBe
                "Failed to restore database. Error: Failed to rename new file to ${mainDatabase.dbName}"
            error.clickOk(async = true)

            // Open a test connection so the tidy-up doesn't freak out about the db already being
            // shut down
            t!!.join()
            remoteDb.testConnection()
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
            waitForAssertion { findErrorDialog() shouldNotBe null }
            getErrorDialog().clickOk(async = true)

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
            waitForAssertion { findInfoDialog() shouldNotBe null }

            val summary = "\n\nGames pushed: 1\n\nGames pulled: 1"
            val expectedInfoText = "Sync completed successfully!$summary"
            val info = getInfoDialog()
            info.getDialogMessage() shouldBe expectedInfoText
            info.clickOk(async = true)

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
