package dartzee.helper

import dartzee.db.SyncAuditEntity
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import dartzee.sync.SYNC_DIR
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.file.shouldNotExist
import io.mockk.mockk
import io.mockk.verify
import java.io.File

const val REMOTE_NAME = "Goomba"

fun shouldUpdateSyncSummary(testFn: () -> Unit)
{
    val menuScreen = mockk<MenuScreen>(relaxed = true)
    ScreenCache.hmClassToScreen[MenuScreen::class.java] = menuScreen

    mainDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)

    testFn()

    verify { menuScreen.refreshSummary(any()) }
}

fun syncDirectoryShouldNotExist()
{
    File(SYNC_DIR).shouldNotExist()
}

fun makeSyncAudit(database: Database) = SyncAuditEntity(database).also {
    it.assignRowId()
    it.remoteName = REMOTE_NAME
}