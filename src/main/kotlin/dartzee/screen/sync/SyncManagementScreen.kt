package dartzee.screen.sync

import dartzee.db.SyncAuditEntity
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.mainDatabase
import java.awt.BorderLayout

class SyncManagementScreen : EmbeddedScreen() {
    private val setupPanel = SyncSetupPanel()
    private val managementPanel = SyncManagementPanel()

    override fun initialise() {
        remove(setupPanel)
        remove(managementPanel)

        val lastSyncData = SyncAuditEntity.getLastSyncData(mainDatabase)
        if (lastSyncData == null) {
            add(setupPanel, BorderLayout.CENTER)
        } else {
            managementPanel.updateStatus(lastSyncData)
            add(managementPanel, BorderLayout.CENTER)
        }

        ScreenCache.mainScreen.pack()
        repaint()
    }

    override fun getScreenName() = "Sync Management"
}
