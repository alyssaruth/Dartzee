package dartzee.screen.game

import dartzee.clickButton
import dartzee.helper.AbstractTest
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestTutorialPanel: AbstractTest() {
    @Test
    fun `Should launch the game when ready button is pressed`() {
        val parentWindow = mockk<DartsGameScreen>(relaxed = true)
        val panel = TutorialPanel(parentWindow)
        panel.clickButton(text = "I'm ready - let's play!")

        verify { parentWindow.startNewGame() }
    }

    @Test
    fun `Should accurately record busts in the demo`() {
        val panel = TutorialPanel(mockk(relaxed = true))
    }
}