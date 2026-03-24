package dartzee.theme

import com.github.alyssaburlton.swingtest.shouldMatch
import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import io.kotest.matchers.shouldBe
import java.awt.Color
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.border.EtchedBorder
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ThemeTest : AbstractTest() {
    @Test
    @Tag("screenshot")
    fun `Should apply theme`() {
        val theme =
            Theme(
                ThemeId.Easter,
                Color.magenta,
                Color.pink,
                Color.decode("#C9A0DC"),
                Color.decode("#D8BFD8"),
                DEFAULT_COLOUR_WRAPPER,
                Color.BLUE,
            )

        theme.apply()

        val panel = JPanel()
        panel.layout = null
        panel.size = Dimension(200, 200)

        val button = JButton()
        button.size = Dimension(80, 30)
        button.setLocation(60, 150)
        panel.add(button)

        val subPanel = JPanel()
        subPanel.border = EtchedBorder(EtchedBorder.RAISED, null, null)
        subPanel.size = Dimension(100, 80)
        subPanel.setLocation(50, 50)
        panel.add(subPanel)

        val rdbtnOne = JRadioButton()
        rdbtnOne.size = Dimension(80, 20)
        rdbtnOne.setLocation(10, 10)
        rdbtnOne.isSelected = true
        val rdbtnTwo = JRadioButton()
        rdbtnTwo.size = Dimension(80, 20)
        rdbtnTwo.setLocation(10, 30)

        subPanel.add(rdbtnOne)
        subPanel.add(rdbtnTwo)

        panel.shouldMatchImage("themedPanel")
    }

    @Test
    fun `Should load dartboard font`() {
        Themes.OKTOBERFEST.dartboardColourWrapper!!.font shouldBe
            fontForResource("/theme/oktoberfest/dartboard.ttf")
    }

    @Test
    fun `Should return an imageIcon if an icon resource is found`() {
        val expected =
            ImageIcon(javaClass.getResource("/theme/halloween/buttons/playerManagement.png"))
        Themes.HALLOWEEN.icon("/buttons/playerManagement.png")!!.shouldMatch(expected)
    }

    @Test
    fun `Should return null if no resource is found`() {
        Themes.HALLOWEEN.icon("/buttons/blah.png") shouldBe null
    }
}
