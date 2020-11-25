package dartzee.helper

import dartzee.screen.ScreenCache
import dartzee.screen.sync.SyncSummaryPanel
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.mainDatabase
import io.mockk.mockk
import io.mockk.verify

fun shouldUpdateSyncSummary(testFn: () -> Unit)
{
    mainDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION)
    val summaryPanelMock = mockk<SyncSummaryPanel>(relaxed = true)
    ScreenCache.syncSummaryPanel = summaryPanelMock

    testFn()

    verify { summaryPanelMock.refreshSummary(any()) }
}
