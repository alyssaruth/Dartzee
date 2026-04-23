package dartzee.theme

import com.github.alyssaburlton.swingtest.shouldMatch
import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.helper.makeTheme
import dartzee.utils.InjectedThings
import io.kotest.matchers.shouldBe
import java.awt.Color
import java.awt.Dimension
import java.net.URL
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
            makeTheme(
                ThemeId.Easter,
                primary = Color.magenta,
                primaryDark = Color.pink,
                background = Color.decode("#C9A0DC"),
                lightBackground = Color.decode("#D8BFD8"),
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

    @Test
    fun `Should make use of custom icon functions`() {
        val location = javaClass.getResource("/theme/halloween/buttons/playerManagement.png")
        val fn =
            fun(): URL? {
                return location
            }

        val theme = makeTheme(customIcons = mapOf("/buttons/newGame.png" to fn))

        theme.icon("/buttons/newGame.png")!!.shouldMatch(ImageIcon(location))
        theme.icon("/buttons/preferences.png") shouldBe null
    }

    @Test
    fun `Should be able to override buttons based on text and name`() {
        val panel = JPanel()

        val buttonOk = JButton("Ok")
        val buttonDeleteGame = JButton().also { it.name = "deleteGame" }
        val buttonOkTwo = JButton("OK")
        val buttonOther = JButton("Other").also { it.name = "something" }

        val originalBg = buttonOther.background

        panel.add(buttonOk)
        panel.add(buttonDeleteGame)
        panel.add(buttonOkTwo)
        panel.add(buttonOther)

        val buttonOverrideColours = mapOf("ok" to Color.green, "deletegame" to Color.red)

        InjectedThings.theme = makeTheme(buttonOverrideColours = buttonOverrideColours)

        panel.applyButtonOverrides()

        buttonOk.background shouldBe Color.green
        buttonOkTwo.background shouldBe Color.green
        buttonDeleteGame.background shouldBe Color.red
        buttonOther.background shouldBe originalBg
    }
}
