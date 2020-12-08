package dartzee.sync

import dartzee.db.SyncAuditEntity
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import org.junit.Test

class TestSyncUtils: AbstractTest()
{
    @Test
    fun `Should return an empty string if no remote db name set`()
    {
        getRemoteName() shouldBe ""
    }

    @Test
    fun `Should be able to save and retrieve the remote db name`()
    {
        SyncAuditEntity.insertSyncAudit(mainDatabase, "foobar")
        getRemoteName() shouldBe "foobar"
    }
}