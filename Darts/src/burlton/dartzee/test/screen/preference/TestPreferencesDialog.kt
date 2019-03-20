package burlton.dartzee.test.screen.preference

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.code.screen.preference.AbstractPreferencesPanel
import burlton.dartzee.code.screen.preference.PreferencesDialog
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.awt.event.ActionEvent

class TestPreferencesDialog
{
    var dialog = PreferencesDialog()
    var mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)

    @Before
    fun setup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())

        dialog = PreferencesDialog()
        mockPanel = mockk(relaxed = true)

        dialog.tabbedPane.add(mockPanel)
    }

    @Test
    fun `Should refresh panels on init`()
    {
        dialog.init()

        verify { mockPanel.refresh(false) }
    }

    @Test
    fun `Shouldn't call save if a panel is invalid`()
    {
        every { mockPanel.valid() } returns false

        val actionEvent = mockk<ActionEvent>(relaxed = true)
        every {actionEvent.source } returns dialog.btnOk

        dialog.actionPerformed(actionEvent)

        verify { mockPanel.valid() }
        verify(exactly = 0) { mockPanel.save() }

        dialog.tabbedPane.selectedComponent shouldBe mockPanel
    }

    @Test
    fun `Should call save if all panels are valid`()
    {
        every { mockPanel.valid() } returns true

        val actionEvent = mockk<ActionEvent>(relaxed = true)
        every {actionEvent.source } returns dialog.btnOk

        dialog.init()
        dialog.actionPerformed(actionEvent)

        verify { mockPanel.valid() }
        verify { mockPanel.save() }
    }

    @Test
    fun `Should not reset unselected panels when restore defaults is pressed`()
    {
        val actionEvent = mockk<ActionEvent>(relaxed = true)
        every {actionEvent.source } returns dialog.btnRestoreDefaults

        dialog.actionPerformed(actionEvent)

        verify(exactly = 0) { mockPanel.refresh(true) }
    }

    @Test
    fun `Should reset the selected panel when restore defaults is pressed`()
    {
        val actionEvent = mockk<ActionEvent>(relaxed = true)
        every {actionEvent.source } returns dialog.btnRestoreDefaults

        dialog.tabbedPane.selectedComponent = mockPanel
        dialog.actionPerformed(actionEvent)

        verify { mockPanel.refresh(true) }
    }

    @Test
    fun `Should reset cached application values when successfully okayed`()
    {
        every { mockPanel.valid() } returns true

        val actionEvent = mockk<ActionEvent>(relaxed = true)
        every {actionEvent.source } returns dialog.btnOk

        Dartboard.dartboardTemplate = mockk(relaxed = true)
        val mockGameScreen = mockk<DartsGameScreen>(relaxed = true)
        ScreenCache.addDartsGameScreen("1", mockGameScreen)

        dialog.init()
        dialog.actionPerformed(actionEvent)

        Dartboard.dartboardTemplate shouldBe null
        verify { mockGameScreen.fireAppearancePreferencesChanged() }
    }

    @Test
    fun `Should not call valid or save when cancelled`()
    {
        val actionEvent = mockk<ActionEvent>(relaxed = true)
        every {actionEvent.source } returns dialog.btnCancel

        dialog.actionPerformed(actionEvent)

        verify(exactly = 0) { mockPanel.valid() }
        verify(exactly = 0) { mockPanel.save() }
    }
}
