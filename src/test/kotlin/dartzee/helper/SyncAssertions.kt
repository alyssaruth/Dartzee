package dartzee.helper

import dartzee.screen.ScreenCache
import dartzee.screen.sync.SyncSummaryPanel
import dartzee.sync.SYNC_DIR
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.file.shouldNotExist
import io.mockk.mockk
import io.mockk.verify
import java.io.File

const val REMOTE_NAME = "Goomba"

fun shouldUpdateSyncSummary(testFn: () -> Unit)
{
    val originalPanel = ScreenCache.syncSummaryPanel

    try
    {
        mainDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)
        val summaryPanelMock = mockk<SyncSummaryPanel>(relaxed = true)
        ScreenCache.syncSummaryPanel = summaryPanelMock

        testFn()

        verify { summaryPanelMock.refreshSummary(any()) }
    }
    finally
    {
        ScreenCache.syncSummaryPanel = originalPanel
    }
}

fun syncDirectoryShouldNotExist()
{
    File(SYNC_DIR).shouldNotExist()
}