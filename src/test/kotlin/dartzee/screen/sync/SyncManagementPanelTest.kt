package dartzee.screen.sync

import dartzee.CURRENT_TIME
import dartzee.PAST_TIME
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.formatTimestamp
import dartzee.db.SyncAuditEntity
import dartzee.findErrorDialog
import dartzee.findQuestionDialog
import dartzee.getDialogMessage
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.helper.REMOTE_NAME_2
import dartzee.helper.insertGame
import dartzee.helper.makeSyncAudit
import dartzee.screen.ScreenCache
import dartzee.sync.LastSyncData
import dartzee.sync.SyncManager
import dartzee.sync.resetRemote
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.github.alyssaruth.swingtest.clickChild
import io.github.alyssaruth.swingtest.clickNo
import io.github.alyssaruth.swingtest.clickYes
import io.github.alyssaruth.swingtest.expectErrorDialog
import io.github.alyssaruth.swingtest.getChild
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.sql.Timestamp
import java.time.Duration
import javax.swing.JButton
import javax.swing.JLabel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncManagementPanelTest : AbstractTest() {
    private val syncManager = mockk<SyncManager>(relaxed = true)

    @BeforeEach
    fun before() {
        InjectedThings.syncManager = syncManager

        SyncAuditEntity.insertSyncAudit(mainDatabase, REMOTE_NAME)
    }

    /** Status Panel */
    @Test
    fun `Should render the correct remote name, last synced date and pending game count`() {
        setUpModifiedGames(2)
        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(PAST_TIME))

        val panel = SyncManagementPanel()
        panel.updateStatus(data)

        panel.sharedDatabaseLabel().text shouldBe
            "<html><b>Shared Database:</b> $REMOTE_NAME_2</html>"
        panel
            .lastSyncedLabel()
            .text
            .shouldContain("<b>Last Synced:</b> ${Timestamp.from(PAST_TIME).formatTimestamp()}")
        panel.pendingGamesLabel().text.shouldContain("<b>Pending Games:</b> 2")
    }

    @Test
    fun `Should render pending games in green if none pending`() {
        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(PAST_TIME))
        val panel = SyncManagementPanel()
        panel.updateStatus(data)

        panel.pendingGamesLabel().text.shouldContain("color=\"green\"")
    }

    @Test
    fun `Should render pending games in orange if 1 pending`() {
        setUpModifiedGames(1)

        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(PAST_TIME))
        val panel = SyncManagementPanel()
        panel.updateStatus(data)

        panel.pendingGamesLabel().text.shouldContain("color=\"orange\"")
    }

    @Test
    fun `Should render pending games in orange if 9 pending`() {
        setUpModifiedGames(9)

        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(PAST_TIME))
        val panel = SyncManagementPanel()
        panel.updateStatus(data)

        panel.pendingGamesLabel().text.shouldContain("color=\"orange\"")
    }

    @Test
    fun `Should render pending games in red if 10 or more pending`() {
        setUpModifiedGames(10)

        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(PAST_TIME))
        val panel = SyncManagementPanel()
        panel.updateStatus(data)

        panel.pendingGamesLabel().text.shouldContain("color=\"red\"")
    }

    @Test
    fun `Should render last synced in green if within last 24 hours`() {
        val syncTime = CURRENT_TIME.minus(Duration.ofHours(24))
        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(syncTime))

        val panel = SyncManagementPanel()
        panel.updateStatus(data)
        panel.lastSyncedLabel().text.shouldContain("color=\"green\"")
    }

    @Test
    fun `Should render last synced in orange if within last week`() {
        val syncTime = CURRENT_TIME.minus(Duration.ofDays(4))
        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(syncTime))

        val panel = SyncManagementPanel()
        panel.updateStatus(data)
        panel.lastSyncedLabel().text.shouldContain("color=\"orange\"")
    }

    @Test
    fun `Should render last synced in red if over a week ago`() {
        val syncTime = CURRENT_TIME.minus(Duration.ofDays(8))
        val data = LastSyncData(REMOTE_NAME_2, Timestamp.from(syncTime))

        val panel = SyncManagementPanel()
        panel.updateStatus(data)
        panel.lastSyncedLabel().text.shouldContain("color=\"red\"")
    }

    private fun setUpModifiedGames(count: Int) {
        resetRemote()
        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp.from(PAST_TIME))

        repeat(count) { insertGame(dtLastUpdate = Timestamp.from(CURRENT_TIME)) }
    }

    private fun SyncManagementPanel.sharedDatabaseLabel() =
        getChild<JLabel> { it.text.contains("Shared Database") }

    private fun SyncManagementPanel.lastSyncedLabel() =
        getChild<JLabel> { it.text.contains("Last Synced") }

    private fun SyncManagementPanel.pendingGamesLabel() =
        getChild<JLabel> { it.text.contains("Pending Games") }

    @Test
    fun `Should validate no open games before pushing`() {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Push")

        expectErrorDialog("You must close all open games before performing this action.")
        verifyNotCalled { syncManager.doPush(any()) }
    }

    @Test
    fun `Should not push if confirmation is cancelled`() {
        every { syncManager.databaseExists(REMOTE_NAME) } returns true

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Push")

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to push to $REMOTE_NAME? \n\nThis will overwrite any data that hasn't been synced to this device."
        question.clickNo()

        verifyNotCalled { syncManager.doPush(any()) }
    }

    @Test
    fun `Should overwrite remote if push is confirmed`() {
        every { syncManager.databaseExists(REMOTE_NAME) } returns true

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Push")

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to push to $REMOTE_NAME? \n\nThis will overwrite any data that hasn't been synced to this device."
        question.clickYes()

        verify { syncManager.doPush(REMOTE_NAME) }
    }

    @Test
    fun `Should push without confirmation if no remote version exists`() {
        every { syncManager.databaseExists(REMOTE_NAME) } returns false
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Push")

        findQuestionDialog().shouldBeNull()
        verify { syncManager.doPush(REMOTE_NAME) }
    }

    /** Pull */
    @Test
    fun `Should validate no open games before pulling`() {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Pull")

        expectErrorDialog("You must close all open games before performing this action.")
        verifyNotCalled { syncManager.doPull(any()) }
    }

    @Test
    fun `Should not pull if confirmation is cancelled`() {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Pull")

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to pull from $REMOTE_NAME? \n\nThis will overwrite any local data that hasn't been synced to $REMOTE_NAME from this device."
        question.clickNo()

        verifyNotCalled { syncManager.doPull(any()) }
    }

    @Test
    fun `Should overwrite local if pull is confirmed`() {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Pull")

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to pull from $REMOTE_NAME? \n\nThis will overwrite any local data that hasn't been synced to $REMOTE_NAME from this device."
        question.clickYes()

        verify { syncManager.doPull(REMOTE_NAME) }
    }

    /** Sync */
    @Test
    fun `Should validate no open games before syncing`() {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Perform Sync")

        expectErrorDialog("You must close all open games before performing this action.")
        verifyNotCalled { syncManager.doSyncIfNecessary(any()) }
    }

    @Test
    fun `Should carry out a sync`() {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Perform Sync")

        findErrorDialog().shouldBeNull()
        verify { syncManager.doSyncIfNecessary(REMOTE_NAME) }
    }

    /** Reset */
    @Test
    fun `Should not carry out a reset if cancelled`() {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Reset")

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $REMOTE_NAME, requiring you to set it up again."
        question.clickNo()

        SyncAuditEntity.getLastSyncData(mainDatabase).shouldNotBeNull()
    }

    @Test
    fun `Should reset if confirmed`() {
        val panel = makeSyncManagementPanel()
        panel.clickChild<JButton>(text = "Reset")

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to reset?\n\nThis will not delete any local data, but will sever the link with $REMOTE_NAME, requiring you to set it up again."
        question.clickYes()

        SyncAuditEntity.getLastSyncData(mainDatabase).shouldBeNull()
    }

    private fun makeSyncManagementPanel(): SyncManagementPanel {
        val panel = SyncManagementPanel()
        panel.updateStatus(SyncAuditEntity.getLastSyncData(mainDatabase)!!)
        return panel
    }
}
