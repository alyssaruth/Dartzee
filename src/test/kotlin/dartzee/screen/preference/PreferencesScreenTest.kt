package dartzee.screen.preference

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickYes
import dartzee.findQuestionDialog
import dartzee.getDialogMessage
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
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

        screen.clickChild<JButton>("Back", async = true)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to go back?\n\nYou have unsaved changes that will be discarded."
        question.clickNo(async = true)

        ScreenCache.currentScreen() shouldBe screen
    }

    @Test
    fun `Should go back if there are outstanding changes and user confirms`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns true

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.clickChild<JButton>("Back", async = true)
        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to go back?\n\nYou have unsaved changes that will be discarded."
        question.clickYes(async = true)

        ScreenCache.currentScreen().shouldBeInstanceOf<MenuScreen>()
    }

    @Test
    fun `Should go back as normal if there are no outstanding changes`() {
        val mockPanel = mockk<AbstractPreferencesPanel>(relaxed = true)
        every { mockPanel.hasOutstandingChanges() } returns false

        val screen = PreferencesScreen(listOf(mockPanel))

        ScreenCache.switch(screen, true)

        screen.clickChild<JButton>("Back", async = true)
        findQuestionDialog().shouldBeNull()

        ScreenCache.currentScreen().shouldBeInstanceOf<MenuScreen>()
    }
}
