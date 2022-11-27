package dartzee.bean

import dartzee.`object`.DartsClient
import dartzee.helper.AbstractTest
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSliderAiSpeed: AbstractTest()
{
    @Test
    fun `Should set custom UI on non-apple OS`()
    {
        DartsClient.operatingSystem = "Linux"
        val slider = SliderAiSpeed(true)
        slider.ui.shouldBeInstanceOf<CustomSliderUI>()
    }

    @Test
    fun `Should leave default UI on apple OS`()
    {
        DartsClient.operatingSystem = "mac"
        val slider = SliderAiSpeed(true)
        slider.ui.shouldNotBeInstanceOf<CustomSliderUI>()
    }

    @Test
    fun `Should leave the default UI if passed false`()
    {
        DartsClient.operatingSystem = "Linux"
        val slider = SliderAiSpeed(false)
        slider.ui.shouldNotBeInstanceOf<CustomSliderUI>()
    }

    @Test
    fun `Should initialise with the correct slider bounds`()
    {
        val slider = SliderAiSpeed(true)
        slider.maximum shouldBe AI_SPEED_MAXIMUM
        slider.minimum shouldBe AI_SPEED_MINIMUM
        slider.inverted shouldBe true
    }
}