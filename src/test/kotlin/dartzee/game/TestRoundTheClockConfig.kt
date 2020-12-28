package dartzee.game

import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestRoundTheClockConfig: AbstractTest()
{
    @Test
    fun `Should serialize and deserialize correctly`()
    {
        val config = RoundTheClockConfig(ClockType.Doubles, true)
        val json = config.toJson()

        val newConfig = RoundTheClockConfig.fromJson(json)
        newConfig shouldBe config
    }

    @Test
    fun `Should deserialize correctly`()
    {
        val json = """{ "clockType": "Trebles", "inOrder": false }"""
        val config = RoundTheClockConfig.fromJson(json)
        config.inOrder shouldBe false
        config.clockType shouldBe ClockType.Trebles
    }
}