package dartzee.screen.sync

import com.github.alexburlton.swingtest.clickChild
import dartzee.core.helper.verifyNotCalled
import dartzee.db.SyncAuditEntity
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.screen.ScreenCache
import dartzee.sync.InMemoryRemoteDatabaseStore
import dartzee.sync.SyncManager
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.swing.JButton
import javax.swing.JOptionPane

class TestSyncManagementPanel: AbstractTest()
{
    private val syncManager = mockk<SyncManager>(relaxed = true)
    private val remoteDatabaseStore = InMemoryRemoteDatabaseStore()

    @BeforeEach
    fun before()
    {
        remoteDatabaseStore.clear()

        InjectedThings.syncManager = syncManager
        InjectedThings.remoteDatabaseStore = remoteDatabaseStore
        
        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
    }

    /**
     * Push
     */
    @Test
    fun `Should validate no open games before pushing`()
    {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Push")

        dialogFactory.errorsShown.shouldContainExactly("You must close all open games before performing this action.")
        verifyNotCalled { syncManager.doPush(any()) }
    }

    @Test
    fun `Should not push if confirmation is cancelled`()
    {
        dialogFactory.questionOption = JOptionPane.NO_OPTION
        remoteDatabaseStore.pushDatabase(REMOTE_NAME, mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Push")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to push to $REMOTE_NAME? \n\nThis will overwrite any data that hasn't been synced to this device.")
        verifyNotCalled { syncManager.doPush(any()) }
    }

    @Test
    fun `Should overwrite remote if push is confirmed`()
    {
        dialogFactory.questionOption = JOptionPane.YES_OPTION
        remoteDatabaseStore.pushDatabase(REMOTE_NAME, mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Push")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to push to $REMOTE_NAME? \n\nThis will overwrite any data that hasn't been synced to this device.")
        verify { syncManager.doPush(REMOTE_NAME) }
    }

    @Test
    fun `Should push without confirmation if no remote version exists`()
    {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Push")

        dialogFactory.questionsShown.shouldBeEmpty()
        verify { syncManager.doPush(REMOTE_NAME) }
    }

    /**
     * Pull
     */
    @Test
    fun `Should validate no open games before pulling`()
    {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Pull")

        dialogFactory.errorsShown.shouldContainExactly("You must close all open games before performing this action.")
        verifyNotCalled { syncManager.doPull(any()) }
    }

    @Test
    fun `Should not pull if confirmation is cancelled`()
    {
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Pull")

        dialogFactory.questionsShown.shouldContainExactly(
                "Are you sure you want to pull from $REMOTE_NAME? \n\nThis will overwrite any local data that hasn't been synced to $REMOTE_NAME from this device.")
        verifyNotCalled { syncManager.doPull(any()) }
    }

    @Test
    fun `Should overwrite local if pull is confirmed`()
    {
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Pull")

        dialogFactory.questionsShown.shouldContainExactly(
                "Are you sure you want to pull from $REMOTE_NAME? \n\nThis will overwrite any local data that hasn't been synced to $REMOTE_NAME from this device.")
        verify { syncManager.doPull(REMOTE_NAME) }
    }

    /**
     * Sync
     */
    @Test
    fun `Should validate no open games before syncing`()
    {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Perform Sync")

        dialogFactory.errorsShown.shouldContainExactly("You must close all open games before performing this action.")
        verifyNotCalled { syncManager.doSync(any()) }
    }

    @Test
    fun `Should carry out a sync`()
    {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Perform Sync")

        dialogFactory.errorsShown.shouldBeEmpty()
        verify { syncManager.doSync(REMOTE_NAME) }
    }

    /**
     * Reset
     */
    @Test
    fun `Should not carry out a reset if cancelled`()
    {
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Reset")

        dialogFactory.questionsShown.shouldContainExactly(
                "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $REMOTE_NAME, requiring you to set it up again."
        )

        SyncAuditEntity.getLastSyncData(mainDatabase).shouldNotBeNull()
    }

    @Test
    fun `Should reset if confirmed`()
    {
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>("Reset")

        dialogFactory.questionsShown.shouldContainExactly(
                "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $REMOTE_NAME, requiring you to set it up again."
        )

        SyncAuditEntity.getLastSyncData(mainDatabase).shouldBeNull()
    }

    private fun makeSyncManagementPanel(): SyncManagementPanel
    {
        val panel = SyncManagementPanel()
        panel.updateStatus(SyncAuditEntity.getLastSyncData(mainDatabase)!!)
        return panel
    }
}