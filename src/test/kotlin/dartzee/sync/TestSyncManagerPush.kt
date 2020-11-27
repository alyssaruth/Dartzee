package dartzee.sync

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_PUSH_ERROR
import dartzee.logging.Severity
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.IOException

class TestSyncManagerPush: AbstractTest()
{
    @Test
    fun `Should log an error and dismiss the loading dialog if an error occurs`()
    {
        val exception = IOException("Boom.")
        val dbStore = mockk<IRemoteDatabaseStore>()
        every { dbStore.pushDatabase(any(), any()) } throws exception

        val manager = SyncManager(dbStore)
        val t = manager.doPush("Goomba")
        t.join()

        dialogFactory.loadingsShown.shouldContainExactly("Pushing Goomba...")
        dialogFactory.loadingVisible shouldBe false

        val log = verifyLog(CODE_PUSH_ERROR, Severity.ERROR)
        log.errorObject shouldBe exception

        dialogFactory.errorsShown.shouldContainExactly("An unexpected error occurred - no data has been changed.")
    }

    @Test
    fun `Should insert into sync audit and push the current database`()
    {
        val store = InMemoryRemoteDatabaseStore()
    }
}