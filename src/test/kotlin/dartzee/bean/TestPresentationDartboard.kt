package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.`object`.ColourWrapper
import dartzee.utils.DartsColour
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
        val colourWrapper = ColourWrapper(DartsColour.TRANSPARENT).also { it.edgeColour = Color.BLACK }
        val dartboard = PresentationDartboard(colourWrapper)
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