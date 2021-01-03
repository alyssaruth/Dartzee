package dartzee.sync

import dartzee.db.SyncAuditEntity
import dartzee.helper.*
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.sql.Timestamp

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

    @Test
    fun `Should return the count of games modified since last sync`()
    {
        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(2000))

        insertGame(dtLastUpdate = Timestamp(1500))
        insertGame(dtLastUpdate = Timestamp(2500))
        insertGame(dtLastUpdate = Timestamp(5000))

        getModifiedGameCount() shouldBe 2
    }

    @Test
    fun `Should return all games if never synced before`()
    {
        insertGame(dtLastUpdate = Timestamp(150))
        insertGame(dtLastUpdate = Timestamp(250))
        insertGame(dtLastUpdate = Timestamp(500))

        getModifiedGameCount() shouldBe 3
    }

    @Test
    fun `Should delete all sync audits and refresh sync screen`()
    {
        shouldUpdateSyncScreen {
            mainDatabase.updateDatabaseVersion(16)
            makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(200))
            makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(500))

            resetRemote()

            getCountFromTable("SyncAudit") shouldBe 0
        }
    }

    @Test
    fun `Should allow sync action when no open games`()
    {
        validateSyncAction() shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should not allow sync action if there are open games`()
    {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        validateSyncAction() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You must close all open games before performing this action.")
    }
}