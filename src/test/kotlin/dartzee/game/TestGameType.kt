package dartzee.game

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
}