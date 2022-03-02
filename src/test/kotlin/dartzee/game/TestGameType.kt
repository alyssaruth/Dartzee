package dartzee.game

import dartzee.helper.AbstractTest
import dartzee.helper.insertDartzeeTemplate
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestGameType: AbstractTest()
{
    @Test
    fun `Sensible descriptions when no params`()
    {
        GameType.X01.getDescription() shouldBe "X01"
        GameType.GOLF.getDescription() shouldBe "Golf"
        GameType.ROUND_THE_CLOCK.getDescription() shouldBe "Round the Clock"
        GameType.DARTZEE.getDescription() shouldBe "Dartzee"
    }

    @Test
    fun `Sensible descriptions with params`()
    {
        GameType.X01.getDescription("701") shouldBe "701"
        GameType.GOLF.getDescription("18") shouldBe "Golf - 18 holes"
        GameType.ROUND_THE_CLOCK.getDescription(RoundTheClockConfig(ClockType.Trebles, true).toJson()) shouldBe "Round the Clock - Trebles - in order"
        GameType.DARTZEE.getDescription("ZZZZ") shouldBe "Dartzee"
    }

    @Test
    fun `Dartzee description with valid template`()
    {
        val t = insertDartzeeTemplate(name = "Goomba")
        GameType.DARTZEE.getDescription(t.rowId) shouldBe "Dartzee - Goomba"
    }
}