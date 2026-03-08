package dartzee.sync

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.findErrorDialog
import dartzee.findLoadingDialog
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.helper.TEST_DB_DIRECTORY
import dartzee.helper.shouldUpdateSyncScreen
import dartzee.helper.syncDirectoryShouldNotExist
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.logging.CODE_PULL_ERROR
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.utils.Database
import dartzee.utils.InjectedThings.databaseDirectory
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.io.IOException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestSyncManagerPull : AbstractTest() {
    @BeforeEach
    fun beforeEach() {
        File(TEST_DB_DIRECTORY).mkdirs()
    }

    @AfterEach
    fun afterEach() {
        File(TEST_DB_DIRECTORY).deleteRecursively()
    }

    @Test
    fun `Should log an error and dismiss the loading dialog if an error occurs`() {
        val exception = IOException("Boom.")
        val dbStore = mockk<IRemoteDatabaseStore>()
        every { dbStore.fetchDatabase(any()) } throws exception

        val manager = SyncManager(dbStore)
        var t: Thread? = null
        runAsync { t = manager.doPull(REMOTE_NAME) }
        waitForAssertion { findErrorDialog() shouldNotBe null }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "An unexpected error occurred - no data has been changed."
        error.clickOk(async = true)

        waitForAssertion { t != null }
        t!!.join()

        val log = verifyLog(CODE_PULL_ERROR, Severity.ERROR)
        log.errorObject shouldBe exception

        findLoadingDialog("Pulling Goomba...")!!.shouldNotBeVisible()
        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should abort the pull if test connection cannot be established`() {
        val db = mockk<Database>(relaxed = true)
        every { db.testConnection() } returns false

        val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to db)

        val manager = SyncManager(store)
        var t: Thread? = null
        runAsync { t = manager.doPull(REMOTE_NAME) }
        waitForAssertion { findErrorDialog() shouldNotBe null }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "An error occurred connecting to the remote database."
        error.clickOk(async = true)

        waitForAssertion { t != null }
        t!!.join()
    }

    @Test
    fun `Should abort the pull if some other validation error occurs with the database`() {
        usingInMemoryDatabase { db ->
            val store = InMemoryRemoteDatabaseStore(REMOTE_NAME to db)

            val manager = SyncManager(store)
            var t: Thread? = null
            runAsync { t = manager.doPull(REMOTE_NAME) }
            waitForAssertion { findErrorDialog() shouldNotBe null }

            val error = getErrorDialog()
            error.getDialogMessage() shouldBe "An error occurred connecting to the remote database."
            error.clickOk(async = true)

            waitForAssertion { t != null }
            t!!.join()

            val log = verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
            log.message shouldBe
                "Unable to ascertain remote database version (but could connect) - this is unexpected."
        }
    }

    @Test
    fun `Should pull and swap in remote database`() {
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
    fun `Should update sync screen regardless of an error occurring`() {
        shouldUpdateSyncScreen {
            val exception = IOException("Boom.")
            val dbStore = mockk<IRemoteDatabaseStore>()
            every { dbStore.fetchDatabase(any()) } throws exception

            val manager = SyncManager(dbStore)
            var t: Thread? = null
            runAsync { t = manager.doPull(REMOTE_NAME) }
            waitForAssertion { findErrorDialog() shouldNotBe null }

            getErrorDialog().clickOk(async = true)
            waitForAssertion { t != null }
            t!!.join()

            errorLogged() shouldBe true
        }
    }
}
