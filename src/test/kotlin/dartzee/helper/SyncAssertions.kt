package dartzee.helper

import dartzee.screen.ScreenCache
import dartzee.sync.refreshSyncSummary
import dartzee.sync.saveRemoteName
import io.kotlintest.matchers.string.shouldContain

fun shouldUpdateSyncSummary(testFn: () -> Unit)
{
    saveRemoteName("")
    refreshSyncSummary()

    saveRemoteName("Goomba")
    testFn()

    ScreenCache.syncSummaryPanel.text.shouldContain("Goomba")
}
