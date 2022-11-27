package dartzee.utils

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import javax.swing.JLabel

class TestDartsColour: AbstractTest()
{
    @Test
    fun `Should return a darker version of the supplied colour`()
    {
        val c1 = Color(100, 100, 100)

        val c1Result = DartsColour.getDarkenedColour(c1)

        c1Result.red shouldBe 49
        c1Result.alpha shouldBe c1.alpha
    }

    @Test
    fun `Should set the right fg and bg colours based on finishing position`()
    {
        val component = JLabel()

        DartsColour.setFgAndBgColoursForPosition(component, 1)
        component.foreground shouldBe DartsColour.COLOUR_GOLD_TEXT
        component.background shouldBe Color.YELLOW

        DartsColour.setFgAndBgColoursForPosition(component, 2)
        component.foreground shouldBe DartsColour.COLOUR_SILVER_TEXT
        component.background shouldBe Color.GRAY

        DartsColour.setFgAndBgColoursForPosition(component, 3)
        component.foreground shouldBe DartsColour.COLOUR_BRONZE_TEXT
        component.background shouldBe DartsColour.COLOUR_BRONZE

        DartsColour.setFgAndBgColoursForPosition(component, 4)
        component.foreground shouldBe DartsColour.COLOUR_BRONZE
        component.background shouldBe Color.BLACK

        DartsColour.setFgAndBgColoursForPosition(component, -1, Color.MAGENTA)
        component.foreground shouldBe null
        component.background shouldBe Color.MAGENTA

        DartsColour.setFgAndBgColoursForPosition(component, -1)
        component.foreground shouldBe null
        component.background shouldBe null
    }
}

