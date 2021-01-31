package dartzee.sync

import dartzee.PAST_TIME
import dartzee.db.ParticipantEntity
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

    @Test
    fun `Should consider all relevant entities when checking whether a full sync is required`()
    {
        validateSyncIsNecessary { insertPlayer() }
        validateSyncIsNecessary { insertAchievement() }
        validateSyncIsNecessary { insertGame() }
        validateSyncIsNecessary { insertParticipant() }
        validateSyncIsNecessary { insertDartsMatch() }
        validateSyncIsNecessary { insertDartzeeRule() }
        validateSyncIsNecessary { insertDartzeeTemplate() }
        validateSyncIsNecessary { insertPlayerImage() }
        validateSyncIsNecessary { insertDart(ParticipantEntity()) }
    }
    private fun validateSyncIsNecessary(setupFn: () -> Unit)
    {
        wipeDatabase()
        setupFn()
        needsSync() shouldBe true
    }

    @Test
    fun `Should not perform a sync if there are no local changes`()
    {
        needsSync() shouldBe false

        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
        insertGame(dtLastUpdate = Timestamp.from(PAST_TIME))
        needsSync() shouldBe false
    }

    @Test
    fun `Should perform a sync if changes since last sync date`()
    {
        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
        insertGame()
        needsSync() shouldBe true
    }
}