package dartzee.bean

import com.github.alyssaburlton.swingtest.getChild
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JSlider

class TestSliderAiSpeed: AbstractTest()
{
    @Test
    fun `Should initialise with the correct slider bounds`()
    {
        val aiSlider = SliderAiSpeed()
        val actualSlider = aiSlider.getChild<JSlider>()
        actualSlider.maximum shouldBe AI_SPEED_MAXIMUM
        actualSlider.minimum shouldBe AI_SPEED_MINIMUM
        actualSlider.inverted shouldBe true
    }
}