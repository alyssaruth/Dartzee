package dartzee.helper

import dartzee.db.SyncAuditEntity
import dartzee.screen.ScreenCache
import dartzee.screen.sync.SyncManagementScreen
import dartzee.sync.SYNC_DIR
import dartzee.utils.Database
import io.kotlintest.matchers.file.shouldNotExist
import io.mockk.mockk
import io.mockk.verify
import java.io.File

const val REMOTE_NAME = "Goomba"
const val REMOTE_NAME_2 = "Koopa"

fun shouldUpdateSyncScreen(testFn: () -> Unit)
{
    val menuScreen = mockk<SyncManagementScreen>(relaxed = true)
    ScreenCache.hmClassToScreen[SyncManagementScreen::class.java] = menuScreen

    testFn()

    verify { menuScreen.initialise() }
}

fun syncDirectoryShouldNotExist()
{
    File(SYNC_DIR).shouldNotExist()
}

fun makeSyncAudit(database: Database) = SyncAuditEntity(database).also {
    it.assignRowId()
    it.remoteName = REMOTE_NAME
}