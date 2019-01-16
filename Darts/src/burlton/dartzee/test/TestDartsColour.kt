package burlton.dartzee.test

import burlton.dartzee.code.utils.DartsColour
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.awt.Color
import javax.swing.JLabel
import kotlin.test.assertNull

class TestDartsColour
{
    @Test
    fun testGetDarkenedColor()
    {
        val c1 = Color(100, 100, 100)
        val c2 = null

        val c1Result = DartsColour.getDarkenedColour(c1)
        val c2Result = DartsColour.getDarkenedColour(c2)

        assertThat(c1Result.red, equalTo(49))
        assertThat(c1Result.alpha, equalTo(c1.alpha))
        assertNull(c2Result)
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

        assertThat(componentFirst.foreground, equalTo(DartsColour.COLOUR_GOLD_TEXT))
        assertThat(componentFirst.background, equalTo(Color.YELLOW))

        assertThat(componentSecond.foreground, equalTo(DartsColour.COLOUR_SILVER_TEXT))
        assertThat(componentSecond.background, equalTo(Color.GRAY))

        assertThat(componentThird.foreground, equalTo(DartsColour.COLOUR_BRONZE_TEXT))
        assertThat(componentThird.background, equalTo(DartsColour.COLOUR_BRONZE))

        assertThat(componentFourth.foreground, equalTo(DartsColour.COLOUR_BRONZE))
        assertThat(componentFourth.background, equalTo(Color.BLACK))

        assertNull(componentUnfinishedWithDefault.foreground)
        assertThat(componentUnfinishedWithDefault.background, equalTo(Color.MAGENTA))

        assertNull(componentUnfinished.foreground)
        assertNull(componentUnfinished.background)
    }
}

