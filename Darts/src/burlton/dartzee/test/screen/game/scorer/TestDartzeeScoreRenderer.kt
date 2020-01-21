package burlton.dartzee.test.screen.game.scorer

import burlton.dartzee.code.screen.game.scorer.DartzeeScoreRenderer
import burlton.dartzee.test.helper.AbstractTest
import burlton.dartzee.test.shouldBeBetween
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import javax.swing.SwingConstants

class TestDartzeeScoreRenderer: AbstractTest()
{
    @Test
    fun `Should set the background colour saturation to a percentage of the maximum`()
    {
        val renderer = DartzeeScoreRenderer(50)

        renderer.setCellColours(0, false)
        renderer.background.getSaturation() shouldBe 0f

        renderer.setCellColours(25, false)
        renderer.background.getSaturation().shouldBeBetween(0.49, 0.51)

        renderer.setCellColours(50, false)
        renderer.background.getSaturation().shouldBeBetween(0.99, 1.0)

        renderer.setCellColours(null, false)
        renderer.background shouldBe null
    }

    @Test
    fun `Should just render the value`()
    {
        val renderer = DartzeeScoreRenderer(50)
        renderer.getReplacementValue(20) shouldBe 20
    }

    @Test
    fun `Should set font and alignment, and allow nulls`()
    {
        val renderer = DartzeeScoreRenderer(20)

        renderer.setFontsAndAlignment()
        renderer.font.isBold shouldBe true
        renderer.horizontalAlignment shouldBe SwingConstants.CENTER
        renderer.allowNulls() shouldBe true
    }

    private fun Color.getSaturation(): Float
    {
        val hsb = Color.RGBtoHSB(red, green, blue, null)
        return hsb[1]
    }
}