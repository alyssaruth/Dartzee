package dartzee.screen.animation

import dartzee.helper.AbstractTest
import dartzee.theme.CHRISTMAS
import dartzee.theme.Themes
import dartzee.utils.InjectedThings
import io.github.alyssaruth.swingtest.shouldMatch
import java.time.LocalDate
import javax.swing.ImageIcon
import org.junit.jupiter.api.Test

class AnimationTest : AbstractTest() {
    @Test
    fun `Should use themed override image if it exists`() {
        InjectedThings.now = LocalDate.of(2027, 1, 1)

        val default = ImageIcon(javaClass.getResource("/horrific/dev.png"))
        val christmas = ImageIcon(javaClass.getResource("/theme/christmas/horrific/dev.png"))
        val makeAnimation = { Animation("fourTrimmed", "/horrific/dev.png") }

        makeAnimation().img!!.shouldMatch(default)

        InjectedThings.theme = Themes.CHRISTMAS
        makeAnimation().img!!.shouldMatch(christmas)
    }
}
