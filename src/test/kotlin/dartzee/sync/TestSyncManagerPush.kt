package dartzee.sync

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.db.SyncAuditEntity
import dartzee.findErrorDialog
import dartzee.findLoadingDialog
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.helper.shouldUpdateSyncScreen
import dartzee.helper.syncDirectoryShouldNotExist
import dartzee.logging.CODE_PUSH_ERROR
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import org.junit.jupiter.api.Test

class TestSyncManagerPush : AbstractTest() {
    @Test
    fun `Should log an error, dismiss the loading dialog and tidy up SyncAudit if an error occurs`() {
        val exception = IOException("Boom.")
        val dbStore = mockk<IRemoteDatabaseStore>()
        every { dbStore.pushDatabase(any(), any()) } throws exception

        val manager = SyncManager(dbStore)
        var t: Thread? = null
        runAsync { t = manager.doPush(REMOTE_NAME) }
        waitForAssertion { findErrorDialog() shouldNotBe null }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "An unexpected error occurred - no data has been changed."
        error.clickOk(async = true)

        waitForAssertion { t shouldNotBe null }
        t!!.join()

        findLoadingDialog("Pushing $REMOTE_NAME...")!!.shouldNotBeVisible()

        val log = verifyLog(CODE_PUSH_ERROR, Severity.ERROR)
        log.errorObject shouldBe exception

        SyncAuditEntity.getLastSyncData(mainDatabase) shouldBe null

        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should insert into sync audit and push the current database`() {
        val store = InMemoryRemoteDatabaseStore()

        val manager = SyncManager(store)
        val t = manager.doPush(REMOTE_NAME)
        t.join()
        flushEdt()

        findLoadingDialog("Pushing $REMOTE_NAME...")!!.shouldNotBeVisible()

        store.fetchDatabase(REMOTE_NAME).database shouldBe mainDatabase
        val lastSyncData = SyncAuditEntity.getLastSyncData(mainDatabase)!!
        lastSyncData.remoteName shouldBe REMOTE_NAME

        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should update sync screen regardless of an error occurring`() {
        shouldUpdateSyncScreen {
            val exception = IOException("Boom.")
            val dbStore = mockk<IRemoteDatabaseStore>()
            every { dbStore.pushDatabase(any(), any()) } throws exception

            val manager = SyncManager(dbStore)
            var t: Thread? = null
            runAsync { t = manager.doPush(REMOTE_NAME) }
            waitForAssertion { findErrorDialog() shouldNotBe null }

            getErrorDialog().clickOk(async = true)
            waitForAssertion { t != null }
            t!!.join()

            errorLogged() shouldBe true
        }
    }
}
