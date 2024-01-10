package dartzee.screen.game.scorer

import dartzee.game.ClockType
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.makeDart
import dartzee.`object`.DartNotThrown
import dartzee.utils.PREFERENCES_DOUBLE_BG_BRIGHTNESS
import dartzee.utils.PREFERENCES_DOUBLE_FG_BRIGHTNESS
import dartzee.utils.PreferenceUtil
import io.kotest.matchers.shouldBe
import java.awt.Color
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestRoundTheClockDartRenderer : AbstractRegistryTest() {
    override fun getPreferencesAffected() =
        listOf(PREFERENCES_DOUBLE_FG_BRIGHTNESS, PREFERENCES_DOUBLE_BG_BRIGHTNESS)

    @BeforeEach
    fun beforeEach() {
        clearPreferences()
    }

    @Test
    fun `Should render hits as normal darts, and misses as an X`() {
        val renderer = RoundTheClockDartRenderer(ClockType.Standard)
        val dart = makeDart(1, 1, startingScore = 1)
        renderer.getReplacementValue(dart) shouldBe "1"

        val dart2 = makeDart(2, 2, startingScore = 2)
        renderer.getReplacementValue(dart2) shouldBe "D2"

        val missDart = makeDart(20, 1, startingScore = 3)
        renderer.getReplacementValue(missDart) shouldBe "X"
    }

    @Test
    fun `Should render out of order hits as normal darts`() {
        val renderer = RoundTheClockDartRenderer(ClockType.Standard)
        val dart = makeDart(2, 1, startingScore = 1, clockTargets = (1..20).toList())
        renderer.getReplacementValue(dart) shouldBe "2"
    }

    @Test
    fun `Should take into account clockType when rendering darts`() {
        val renderer = RoundTheClockDartRenderer(ClockType.Doubles)
        val doubleOne = makeDart(1, 2, startingScore = 1)
        val singleOne = makeDart(1, 1, startingScore = 1)

        renderer.getReplacementValue(doubleOne) shouldBe "D1"
        renderer.getReplacementValue(singleOne) shouldBe "X"
    }

    @Test
    fun `Should have no cell colours for a null entry`() {
        val renderer = RoundTheClockDartRenderer(ClockType.Standard)
        renderer.setCellColours(null, false)
        renderer.foreground shouldBe null
        renderer.background shouldBe null
    }

    @Test
    fun `Should render an unthrown dart as a black box`() {
        val renderer = RoundTheClockDartRenderer(ClockType.Standard)
        renderer.setCellColours(DartNotThrown(), false)
        renderer.foreground shouldBe Color.BLACK
        renderer.background shouldBe Color.BLACK
    }

    @Test
    fun `Should render misses in red, hits in green and out of order hits in yellow`() {
        val renderer = RoundTheClockDartRenderer(ClockType.Standard)
        val hit = makeDart(1, 1, startingScore = 1)
        val outOfOrderHit = makeDart(2, 1, startingScore = 1, clockTargets = (1..20).toList())
        val miss = makeDart(20, 1, startingScore = 3)

        renderer.setCellColours(hit, false)
        renderer.foreground shouldBe Color.getHSBColor(0.3f, 1f, 0.5f)
        renderer.background shouldBe Color.getHSBColor(0.3f, 1f, 1f)

        renderer.setCellColours(outOfOrderHit, false)
        renderer.foreground shouldBe Color.getHSBColor(0.15f, 1f, 0.5f)
        renderer.background shouldBe Color.getHSBColor(0.15f, 1f, 1f)

        renderer.setCellColours(miss, false)
        renderer.foreground shouldBe Color.getHSBColor(0f, 1f, 0.5f)
        renderer.background shouldBe Color.getHSBColor(0f, 1f, 1f)
    }

    @Test
    fun `Should adhere to brightness preferences`() {
        PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_FG_BRIGHTNESS, 0.8)
        PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_BG_BRIGHTNESS, 0.1)

        val renderer = RoundTheClockDartRenderer(ClockType.Standard)
        val hit = makeDart(1, 1, startingScore = 1)
        renderer.setCellColours(hit, false)
        renderer.foreground shouldBe Color.getHSBColor(0.3f, 1f, 0.8f)
        renderer.background shouldBe Color.getHSBColor(0.3f, 1f, 0.1f)
    }
}
