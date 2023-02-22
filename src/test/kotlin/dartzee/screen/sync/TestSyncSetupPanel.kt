package dartzee.screen.sync

import com.github.alyssaburlton.swingtest.clickChild
import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.screen.ScreenCache
import dartzee.sync.SyncConfig
import dartzee.sync.SyncConfigurer
import dartzee.sync.SyncManager
import dartzee.sync.SyncMode
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import javax.swing.JButton

class TestSyncSetupPanel: AbstractTest()
{
    @Test
    fun `Should validate there are no open games`()
    {
        ScreenCache.addDartsGameScreen("foo", mockk(relaxed = true))

        val configurer = mockk<SyncConfigurer>(relaxed = true)
        InjectedThings.syncConfigurer = configurer

        val panel = SyncSetupPanel()
        panel.clickChild<JButton>("Get Started > ")

        dialogFactory.errorsShown.shouldContainExactly("You must close all open games before performing this action.")
        verifyNotCalled { configurer.doFirstTimeSetup() }
    }

    @Test
    fun `Should not perform any sync actions if input is cancelled`()
    {
        val configurer = mockk<SyncConfigurer>(relaxed = true)
        every { configurer.doFirstTimeSetup() } returns null
        val syncManager = mockk<SyncManager>(relaxed = true)
        InjectedThings.syncManager = syncManager
        InjectedThings.syncConfigurer = configurer

        val panel = SyncSetupPanel()
        panel.clickChild<JButton>("Get Started > ")

        verifyNotCalled { syncManager.doPull(any()) }
        verifyNotCalled { syncManager.doPush(any()) }
        verifyNotCalled { syncManager.doSync(any()) }
    }

    @Test
    fun `Should perform a pull`()
    {
        val configurer = mockk<SyncConfigurer>(relaxed = true)
        every { configurer.doFirstTimeSetup() } returns SyncConfig(SyncMode.OVERWRITE_LOCAL, REMOTE_NAME)
        val syncManager = mockk<SyncManager>(relaxed = true)
        InjectedThings.syncManager = syncManager
        InjectedThings.syncConfigurer = configurer

        val panel = SyncSetupPanel()
        panel.clickChild<JButton>("Get Started > ")

        verify { syncManager.doPull(REMOTE_NAME) }
    }

    @Test
    fun `Should perform a push`()
    {
        val configurer = mockk<SyncConfigurer>(relaxed = true)
        every { configurer.doFirstTimeSetup() } returns SyncConfig(SyncMode.CREATE_REMOTE, REMOTE_NAME)
        val syncManager = mockk<SyncManager>(relaxed = true)
        InjectedThings.syncManager = syncManager
        InjectedThings.syncConfigurer = configurer

        val panel = SyncSetupPanel()
        panel.clickChild<JButton>("Get Started > ")

        verify { syncManager.doPush(REMOTE_NAME) }
    }

    @Test
    fun `Should perform a sync`()
    {
        val configurer = mockk<SyncConfigurer>(relaxed = true)
        every { configurer.doFirstTimeSetup() } returns SyncConfig(SyncMode.NORMAL_SYNC, REMOTE_NAME)
        val syncManager = mockk<SyncManager>(relaxed = true)
        InjectedThings.syncManager = syncManager
        InjectedThings.syncConfigurer = configurer

        val panel = SyncSetupPanel()
        panel.clickChild<JButton>("Get Started > ")

        verify { syncManager.doSync(REMOTE_NAME) }
    }
}