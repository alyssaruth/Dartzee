package dartzee.sync

import dartzee.db.SyncAuditEntity
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.helper.shouldUpdateSyncSummary
import dartzee.helper.syncDirectoryShouldNotExist
import dartzee.logging.CODE_PUSH_ERROR
import dartzee.logging.Severity
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.IOException

class TestSyncManagerPush: AbstractTest()
{
    @Test
    fun `Should log an error, dismiss the loading dialog and tidy up SyncAudit if an error occurs`()
    {
        val exception = IOException("Boom.")
        val dbStore = mockk<IRemoteDatabaseStore>()
        every { dbStore.pushDatabase(any(), any()) } throws exception

        val manager = SyncManager(dbStore)
        val t = manager.doPush(REMOTE_NAME)
        t.join()

        dialogFactory.loadingsShown.shouldContainExactly("Pushing $REMOTE_NAME...")
        dialogFactory.loadingVisible shouldBe false

        val log = verifyLog(CODE_PUSH_ERROR, Severity.ERROR)
        log.errorObject shouldBe exception

        dialogFactory.errorsShown.shouldContainExactly("An unexpected error occurred - no data has been changed.")
        SyncAuditEntity.getLastSyncDate(mainDatabase, REMOTE_NAME) shouldBe null

        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should insert into sync audit and push the current database`()
    {
        val store = InMemoryRemoteDatabaseStore()

        val manager = SyncManager(store)
        val t = manager.doPush(REMOTE_NAME)
        t.join()

        dialogFactory.loadingsShown.shouldContainExactly("Pushing $REMOTE_NAME...")
        dialogFactory.loadingVisible shouldBe false

        store.fetchDatabase(REMOTE_NAME).database shouldBe mainDatabase
        SyncAuditEntity.getLastSyncDate(mainDatabase, REMOTE_NAME) shouldNotBe null

        syncDirectoryShouldNotExist()
    }

    @Test
    fun `Should update sync summary regardless of an error occurring`()
    {
        shouldUpdateSyncSummary {
            val exception = IOException("Boom.")
            val dbStore = mockk<IRemoteDatabaseStore>()
            every { dbStore.pushDatabase(any(), any()) } throws exception

            val manager = SyncManager(dbStore)
            val t = manager.doPush(REMOTE_NAME)
            t.join()

            errorLogged() shouldBe true
        }
    }
}