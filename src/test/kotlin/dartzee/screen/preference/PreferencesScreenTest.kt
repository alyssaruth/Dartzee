package dartzee.screen.preference

import dartzee.findQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import io.github.alyssaruth.swingtest.clickChild
import io.github.alyssaruth.swingtest.expectQuestionDialog
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.swing.JButton
import org.junit.jupiter.api.Test

class PreferencesScreenTest : AbstractTest() {
    @Test
    fun `Should refresh panels on init`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        val screen = PreferencesScreen(listOf(mockPanel))
        screen.initialise()

        verify { mockPanel.refresh(false) }
    }

    @Test
    fun `Should not go back if there are outstanding changes and user cancels`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns true

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.clickChild<JButton>("Back")

        expectQuestionDialog(
            "Are you sure you want to go back?\nYou have unsaved changes that will be discarded.",
            "No",
        )

        ScreenCache.currentScreen() shouldBe screen
    }

    @Test
    fun `Should go back if there are outstanding changes and user confirms`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns true

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.clickChild<JButton>("Back")
        expectQuestionDialog(
            "Are you sure you want to go back?\nYou have unsaved changes that will be discarded.",
            "Yes",
        )

        ScreenCache.currentScreen().shouldBeInstanceOf<MenuScreen>()
    }

    @Test
    fun `Should go back as normal if there are no outstanding changes`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns false

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.clickChild<JButton>("Back")
        findQuestionDialog().shouldBeNull()

        ScreenCache.currentScreen().shouldBeInstanceOf<MenuScreen>()
    }
}
