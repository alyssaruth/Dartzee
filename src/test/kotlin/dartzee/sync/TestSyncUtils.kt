package dartzee.sync

import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.formatTimestamp
import dartzee.db.SyncAuditEntity
import dartzee.helper.*
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
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
    fun `Should not update Sync Summary if DB version is too old`()
    {
        val menuScreen = mockMenuScreen()
        mainDatabase.updateDatabaseVersion(15)

        refreshSyncSummary()

        verifyNotCalled { menuScreen.refreshSummary(any()) }
    }

    @Test
    fun `Should refresh with blank sync summary if never synced before`()
    {
        val menuScreen = mockMenuScreen()
        mainDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)

        refreshSyncSummary()

        verify { menuScreen.refreshSummary(SyncSummary("Unset", "-", "-")) }
    }

    @Test
    fun `Should refresh with correct data if sync has occurred`()
    {
        val menuScreen = mockMenuScreen()
        mainDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)

        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(200))

        refreshSyncSummary()

        verify { menuScreen.refreshSummary(SyncSummary(REMOTE_NAME, Timestamp(200).formatTimestamp(), "0"))}
    }

    @Test
    fun `Should refresh with correct game count if sync has occurred`()
    {
        val menuScreen = mockMenuScreen()
        mainDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)

        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(2000))

        insertGame(dtLastUpdate = Timestamp(1500))
        insertGame(dtLastUpdate = Timestamp(2500))
        insertGame(dtLastUpdate = Timestamp(5000))

        refreshSyncSummary()

        verify { menuScreen.refreshSummary(SyncSummary(REMOTE_NAME, Timestamp(200).formatTimestamp(), "2"))}
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
    fun `Should delete all sync audits and update summary`()
    {
        val menuScreen = mockMenuScreen()
        mainDatabase.updateDatabaseVersion(16)
        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(200))
        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(500))

        resetRemote()

        getCountFromTable("SyncAudit") shouldBe 0
        verify { menuScreen.refreshSummary(SyncSummary("Unset", "-", "-")) }
    }

    private fun mockMenuScreen(): MenuScreen
    {
        val menuScreen = mockk<MenuScreen>(relaxed = true)
        ScreenCache.hmClassToScreen[MenuScreen::class.java] = menuScreen
        return menuScreen
    }
}