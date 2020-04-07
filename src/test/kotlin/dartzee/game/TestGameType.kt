package dartzee.game

import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

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
        GameType.ROUND_THE_CLOCK.getDescription(CLOCK_TYPE_STANDARD) shouldBe "Round the Clock - Standard"
        GameType.DARTZEE.getDescription("ZZZZ") shouldBe "Dartzee"
    }
}