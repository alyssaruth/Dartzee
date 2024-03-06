package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import io.kotest.matchers.shouldBe
import javax.swing.JButton
import org.junit.jupiter.api.Test

class TestUtilitiesScreen : AbstractTest() {
    @Test
    fun `Should enter party mode`() {
        ScreenCache.switch<UtilitiesScreen>()

        val utilitiesScreen = ScreenCache.currentScreen()
        utilitiesScreen.clickChild<JButton>(text = "Enter Party Mode")

        InjectedThings.partyMode shouldBe true
        ScreenCache.currentScreen() shouldBe ScreenCache.get<MenuScreen>()
    }
}
