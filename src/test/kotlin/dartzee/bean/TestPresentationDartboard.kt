package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.`object`.ColourWrapper
import dartzee.`object`.WIREFRAME_COLOUR_WRAPPER
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.Color

class TestPresentationDartboard : AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should match snapshot - default`()
    {
        val dartboard = PresentationDartboard()
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.shouldMatchImage("default")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - wireframe`()
    {
        val dartboard = PresentationDartboard(WIREFRAME_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 250, 250)
        dartboard.shouldMatchImage("wireframe")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - custom colours`()
    {
        val colourWrapper = ColourWrapper(
            Color.PINK.darker(), Color.PINK, Color.PINK,
            Color.YELLOW.brighter(), Color.YELLOW.darker(), Color.YELLOW.darker(),
            Color.PINK, Color.YELLOW.darker()
        )

        val dartboard = PresentationDartboard(colourWrapper)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.shouldMatchImage("custom")
    }
}