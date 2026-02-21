package dartzee.bean

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import dartzee.core.helper.verifyNotCalled
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import org.junit.jupiter.api.Test

class TestGameParamFilterPanelRoundTheClock : AbstractTest() {
    @Test
    fun `Should select the correct defaults`() {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.getChild<JRadioButton>(text = "Standard").isSelected shouldBe true
        panel.getChild<JCheckBox>(text = "In order").isSelected shouldBe true
    }

    @Test
    fun `Should return game params based on the radio & checkbox selection`() {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.clickChild<JRadioButton>(text = "Standard")
        panel.getConfig() shouldBe RoundTheClockConfig(ClockType.Standard, true)
        panel.getFilterDesc() shouldBe "Standard games (in order)"

        panel.clickChild<JRadioButton>(text = "Doubles")
        panel.clickChild<JCheckBox>(text = "In order")
        panel.getConfig() shouldBe RoundTheClockConfig(ClockType.Doubles, false)
        panel.getFilterDesc() shouldBe "Doubles games (any order)"

        panel.clickChild<JRadioButton>(text = "Trebles")
        panel.getConfig() shouldBe RoundTheClockConfig(ClockType.Trebles, false)
        panel.getFilterDesc() shouldBe "Trebles games (any order)"
    }

    @Test
    fun `Should support setting the selection by gameParams`() {
        val panel = GameParamFilterPanelRoundTheClock()

        val doublesInOrder = RoundTheClockConfig(ClockType.Doubles, true)
        panel.setGameParams(doublesInOrder.toJson())
        panel.getChild<JRadioButton>(text = "Doubles").isSelected shouldBe true
        panel.getChild<JCheckBox>(text = "In order").isSelected shouldBe true

        val treblesAnyOrder = RoundTheClockConfig(ClockType.Trebles, false)
        panel.setGameParams(treblesAnyOrder.toJson())
        panel.getChild<JRadioButton>(text = "Trebles").isSelected shouldBe true
        panel.getChild<JCheckBox>(text = "In order").isSelected shouldBe false

        val standardInOrder = RoundTheClockConfig(ClockType.Standard, true)
        panel.setGameParams(standardInOrder.toJson())
        panel.getChild<JRadioButton>(text = "Standard").isSelected shouldBe true
        panel.getChild<JCheckBox>(text = "In order").isSelected shouldBe true
    }

    @Test
    fun `Should enable and disable its children correctly`() {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.enableChildren(false)
        panel.getChild<JRadioButton>(text = "Standard").shouldBeDisabled()
        panel.getChild<JRadioButton>(text = "Doubles").shouldBeDisabled()
        panel.getChild<JRadioButton>(text = "Trebles").shouldBeDisabled()
        panel.getChild<JCheckBox>(text = "In order").shouldBeDisabled()

        panel.enableChildren(true)
        panel.getChild<JRadioButton>(text = "Standard").shouldBeEnabled()
        panel.getChild<JRadioButton>(text = "Doubles").shouldBeEnabled()
        panel.getChild<JRadioButton>(text = "Trebles").shouldBeEnabled()
        panel.getChild<JCheckBox>(text = "In order").shouldBeEnabled()
    }

    @Test
    fun `Should add and remove action listeners on the radio button panel`() {
        val panel = GameParamFilterPanelRoundTheClock()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.clickChild<JRadioButton>(text = "Doubles")

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.clickChild<JRadioButton>(text = "Trebles")

        verifyNotCalled { listener.actionPerformed(any()) }
    }

    @Test
    fun `Should add and remove action listeners on the checkbox`() {
        val panel = GameParamFilterPanelRoundTheClock()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.clickChild<JCheckBox>(text = "In order")

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.clickChild<JCheckBox>(text = "In order")

        verifyNotCalled { listener.actionPerformed(any()) }
    }

    private fun GameParamFilterPanelRoundTheClock.getConfig() =
        RoundTheClockConfig.fromJson(getGameParams())
}
