package dartzee.bean

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeDisabled
import com.github.alexburlton.swingtest.shouldBeEnabled
import dartzee.core.helper.verifyNotCalled
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JRadioButton

class TestGameParamFilterPanelRoundTheClock: AbstractTest()
{
    @Test
    fun `Should select the correct defaults`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.getChild<JRadioButton>("Standard").isSelected shouldBe true
        panel.getChild<JCheckBox>("In order").isSelected shouldBe true
    }

    @Test
    fun `Should return game params based on the radio & checkbox selection`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.clickChild<JRadioButton>("Standard")
        panel.getConfig() shouldBe RoundTheClockConfig(ClockType.Standard, true)
        panel.getFilterDesc() shouldBe "Standard games (in order)"

        panel.clickChild<JRadioButton>("Doubles")
        panel.clickChild<JCheckBox>("In order")
        panel.getConfig() shouldBe RoundTheClockConfig(ClockType.Doubles, false)
        panel.getFilterDesc() shouldBe "Doubles games (any order)"

        panel.clickChild<JRadioButton>("Trebles")
        panel.getConfig() shouldBe RoundTheClockConfig(ClockType.Trebles, false)
        panel.getFilterDesc() shouldBe "Trebles games (any order)"
    }

    @Test
    fun `Should support setting the selection by gameParams`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        val doublesInOrder = RoundTheClockConfig(ClockType.Doubles, true)
        panel.setGameParams(doublesInOrder.toJson())
        panel.getChild<JRadioButton>("Doubles").isSelected shouldBe true
        panel.getChild<JCheckBox>("In order").isSelected shouldBe true

        val treblesAnyOrder = RoundTheClockConfig(ClockType.Trebles, false)
        panel.setGameParams(treblesAnyOrder.toJson())
        panel.getChild<JRadioButton>("Trebles").isSelected shouldBe true
        panel.getChild<JCheckBox>("In order").isSelected shouldBe false

        val standardInOrder = RoundTheClockConfig(ClockType.Standard, true)
        panel.setGameParams(standardInOrder.toJson())
        panel.getChild<JRadioButton>("Standard").isSelected shouldBe true
        panel.getChild<JCheckBox>("In order").isSelected shouldBe true
    }

    @Test
    fun `Should enable and disable its children correctly`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.enableChildren(false)
        panel.getChild<JRadioButton>("Standard").shouldBeDisabled()
        panel.getChild<JRadioButton>("Doubles").shouldBeDisabled()
        panel.getChild<JRadioButton>("Trebles").shouldBeDisabled()
        panel.getChild<JCheckBox>("In order").shouldBeDisabled()

        panel.enableChildren(true)
        panel.getChild<JRadioButton>("Standard").shouldBeEnabled()
        panel.getChild<JRadioButton>("Doubles").shouldBeEnabled()
        panel.getChild<JRadioButton>("Trebles").shouldBeEnabled()
        panel.getChild<JCheckBox>("In order").shouldBeEnabled()
    }

    @Test
    fun `Should add and remove action listeners on the radio button panel`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.clickChild<JRadioButton>("Doubles")

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.clickChild<JRadioButton>("Trebles")

        verifyNotCalled { listener.actionPerformed(any()) }
    }

    @Test
    fun `Should add and remove action listeners on the checkbox`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.clickChild<JCheckBox>("In order")

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.clickChild<JCheckBox>("In order")

        verifyNotCalled { listener.actionPerformed(any()) }
    }

    private fun GameParamFilterPanelRoundTheClock.getConfig() = RoundTheClockConfig.fromJson(getGameParams())
}