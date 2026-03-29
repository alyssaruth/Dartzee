package dartzee.theme

import com.github.alyssaburlton.swingtest.getChild
import dartzee.helper.AbstractTest
import dartzee.shouldMatch
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.awt.Color
import java.time.LocalDate
import java.time.Month
import javax.swing.JLabel
import javax.swing.JTextPane
import org.junit.jupiter.api.Test

class ThemePanelTest : AbstractTest() {
    @Test
    fun `Should contain stock text and colours for a locked theme`() {
        InjectedThings.now = LocalDate.of(2026, Month.MARCH, 3)

        val panel = ThemePanel(ThemeId.Halloween)
        panel.background shouldBe Color.DARK_GRAY

        panel.nameLabel().text shouldBe "4. Locked"
        panel.nameLabel().font shouldBe ResourceCache.BASE_FONT.deriveFont(34f)
        panel.nameLabel().foreground shouldBe Color.LIGHT_GRAY

        panel.descriptionLabel().text shouldBe "This theme hasn't unlocked yet. Wait and see!"
        panel.descriptionLabel().foreground shouldBe Color.LIGHT_GRAY

        panel.leftIcon().shouldMatch("/theme/locked.png")
        panel.rightIcon().shouldMatch("/theme/locked.png")
    }

    @Test
    fun `Should render correctly for unlocked theme`() {
        InjectedThings.now = LocalDate.of(2026, Month.DECEMBER, 3)

        val panel = ThemePanel(ThemeId.Halloween)
        panel.background shouldBe Themes.HALLOWEEN.background

        panel.nameLabel().text shouldBe "4. Halloween"
        panel.nameLabel().font shouldBe Themes.HALLOWEEN.font!!.deriveFont(34f)
        panel.nameLabel().foreground shouldBe Themes.HALLOWEEN.fontColor

        panel.descriptionLabel().text shouldContain Themes.HALLOWEEN.description
        panel.descriptionLabel().foreground shouldBe Themes.HALLOWEEN.fontColor

        panel.leftIcon().shouldMatch("/theme/halloween/buttons/playerManagement.png")
        panel.rightIcon().shouldMatch("/theme/halloween/buttons/newGame.png")
    }

    @Test
    fun `Should render correctly for no theme (classic)`() {
        val panel = ThemePanel(ThemeId.None)
        panel.background shouldBe null

        panel.nameLabel().text shouldBe "1. Classic (none)"
        panel.nameLabel().font shouldBe ResourceCache.BASE_FONT.deriveFont(34f)
        panel.nameLabel().foreground shouldBe Color.BLACK

        panel.descriptionLabel().text shouldBe CLASSIC_THEME_DESC
        panel.descriptionLabel().foreground shouldBe Color.BLACK

        panel.leftIcon().shouldMatch("/buttons/playerManagement.png")
        panel.rightIcon().shouldMatch("/buttons/newGame.png")
    }

    private fun ThemePanel.nameLabel() = getChild<JLabel>("Name")

    private fun ThemePanel.descriptionLabel() = getChild<JTextPane>("Description")

    private fun ThemePanel.leftIcon() = getChild<JLabel>("LeftIcon").icon

    private fun ThemePanel.rightIcon() = getChild<JLabel>("RightIcon").icon
}
