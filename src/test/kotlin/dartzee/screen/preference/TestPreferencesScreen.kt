package dartzee.screen.preference

import dartzee.helper.AbstractTest
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.swing.JOptionPane
import org.junit.jupiter.api.Test

class TestPreferencesScreen : AbstractTest() {
    @Test
    fun `Should refresh panels on init`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        val screen = PreferencesScreen(listOf(mockPanel))
        screen.initialise()

        verify { mockPanel.refresh(false) }
    }

    @Test
    fun `Should not go back if there are outstanding changes and user cancels`() {
        dialogFactory.questionOption = JOptionPane.NO_OPTION
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns true

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.btnBack.doClick()
        dialogFactory.questionsShown.shouldContainExactly(
            "Are you sure you want to go back?\n\nYou have unsaved changes that will be discarded."
        )

        ScreenCache.currentScreen() shouldBe screen
    }

    @Test
    fun `Should not go back if there are outstanding changes and user confirms`() {
        dialogFactory.questionOption = JOptionPane.YES_OPTION
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns true

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.btnBack.doClick()
        dialogFactory.questionsShown.shouldContainExactly(
            "Are you sure you want to go back?\n\nYou have unsaved changes that will be discarded."
        )

        ScreenCache.currentScreen().shouldBeInstanceOf<MenuScreen>()
    }

    @Test
    fun `Should go back as normal if there are no outstanding changes`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns false

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.btnBack.doClick()
        dialogFactory.questionsShown.shouldBeEmpty()

        ScreenCache.currentScreen().shouldBeInstanceOf<MenuScreen>()
    }
}
