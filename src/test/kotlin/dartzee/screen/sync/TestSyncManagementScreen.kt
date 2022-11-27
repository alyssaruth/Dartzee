package dartzee.screen.sync

import com.github.alexburlton.swingtest.findChild
import dartzee.db.SyncAuditEntity
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.helper.REMOTE_NAME_2
import dartzee.sync.resetRemote
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import javax.swing.JLabel

class TestSyncManagementScreen: AbstractTest()
{
    @Test
    fun `Should show the setup screen if never synced before`()
    {
        val scrn = SyncManagementScreen()
        scrn.initialise()

        scrn.findChild<SyncManagementPanel>().shouldBeNull()
        scrn.findChild<SyncSetupPanel>().shouldNotBeNull()
    }

    @Test
    fun `Should show the config screen if a sync has occurred`()
    {
        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)

        val scrn = SyncManagementScreen()
        scrn.initialise()

        scrn.findChild<SyncManagementPanel>().shouldNotBeNull()
        scrn.findChild<SyncSetupPanel>().shouldBeNull()
    }

    @Test
    fun `Should show the right child after multiple initialisations`()
    {
        val scrn = SyncManagementScreen()
        scrn.initialise()
        scrn.findChild<SyncManagementPanel>().shouldBeNull()
        scrn.findChild<SyncSetupPanel>().shouldNotBeNull()

        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
        scrn.initialise()
        scrn.findChild<SyncManagementPanel>().shouldNotBeNull()
        scrn.findChild<SyncSetupPanel>().shouldBeNull()

        SyncAuditEntity().deleteAll()
        scrn.initialise()
        scrn.findChild<SyncManagementPanel>().shouldBeNull()
        scrn.findChild<SyncSetupPanel>().shouldNotBeNull()
    }

    @Test
    fun `Should update sync management panel when initialised`()
    {
        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
        val scrn = SyncManagementScreen()
        scrn.initialise()
        scrn.findChild<JLabel> { it.text.contains(REMOTE_NAME) }.shouldNotBeNull()

        resetRemote()
        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME_2)
        scrn.initialise()
        scrn.findChild<JLabel> { it.text.contains(REMOTE_NAME_2) }.shouldNotBeNull()
    }
}