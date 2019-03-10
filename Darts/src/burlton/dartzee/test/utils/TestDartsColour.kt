package burlton.dartzee.test.utils

import burlton.dartzee.code.utils.DartsColour
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import javax.swing.JLabel

class TestDartsColour
{
    @Test
    fun testGetDarkenedColor()
    {
        val c1 = Color(100, 100, 100)
        val c2 = null

        val c1Result = DartsColour.getDarkenedColour(c1)
        val c2Result = DartsColour.getDarkenedColour(c2)

        c1Result.red shouldBe 49
        c1Result.alpha shouldBe c1.alpha

        c2Result shouldBe null
    }

    @Test
    fun testSetFgAndBgColoursForPosition()
    {
        val componentFirst = JLabel()
        val componentSecond = JLabel()
        val componentThird = JLabel()
        val componentFourth = JLabel()
        val componentUnfinishedWithDefault = JLabel()
        val componentUnfinished = JLabel()

        DartsColour.setFgAndBgColoursForPosition(componentFirst, 1)
        DartsColour.setFgAndBgColoursForPosition(componentSecond, 2)
        DartsColour.setFgAndBgColoursForPosition(componentThird, 3)
        DartsColour.setFgAndBgColoursForPosition(componentFourth, 4)
        DartsColour.setFgAndBgColoursForPosition(componentUnfinishedWithDefault, -1, Color.MAGENTA)
        DartsColour.setFgAndBgColoursForPosition(componentUnfinished, -1)

        componentFirst.foreground shouldBe DartsColour.COLOUR_GOLD_TEXT
        componentFirst.background shouldBe Color.YELLOW

        componentSecond.foreground shouldBe DartsColour.COLOUR_SILVER_TEXT
        componentSecond.background shouldBe Color.GRAY

        componentThird.foreground shouldBe DartsColour.COLOUR_BRONZE_TEXT
        componentThird.background shouldBe DartsColour.COLOUR_BRONZE

        componentFourth.foreground shouldBe DartsColour.COLOUR_BRONZE
        componentFourth.background shouldBe Color.BLACK

        componentUnfinishedWithDefault.foreground shouldBe null
        componentUnfinishedWithDefault.background shouldBe Color.MAGENTA

        componentUnfinished.foreground shouldBe null
        componentUnfinished.background shouldBe null
    }
}

