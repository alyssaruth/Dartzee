package dartzee.game

import dartzee.helper.AbstractTest
import dartzee.helper.insertDartzeeTemplate
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGameType : AbstractTest() {
    @Test
    fun `Not applicable params`() {
        GameType.X01.getDescription(GAME_PARAMS_NOT_APPLICABLE) shouldBe "N/A"
        GameType.GOLF.getDescription(GAME_PARAMS_NOT_APPLICABLE) shouldBe "Golf - N/A"
        GameType.ROUND_THE_CLOCK.getDescription(GAME_PARAMS_NOT_APPLICABLE) shouldBe
            "Round the Clock - N/A"
        GameType.DARTZEE.getDescription(GAME_PARAMS_NOT_APPLICABLE) shouldBe "Dartzee - N/A"
    }

    @Test
    fun `Sensible descriptions when no params`() {
        GameType.X01.getDescription() shouldBe "X01"
        GameType.GOLF.getDescription() shouldBe "Golf"
        GameType.ROUND_THE_CLOCK.getDescription() shouldBe "Round the Clock"
        GameType.DARTZEE.getDescription() shouldBe "Dartzee"
    }

    @Test
    fun `Sensible descriptions with params`() {
        val x01Config = X01Config(701, FinishType.Any)
        GameType.X01.getDescription(x01Config.toJson()) shouldBe x01Config.description()
        GameType.GOLF.getDescription("18") shouldBe "Golf - 18 holes"
        GameType.ROUND_THE_CLOCK.getDescription(
            RoundTheClockConfig(ClockType.Trebles, true).toJson()
        ) shouldBe "Round the Clock - Trebles - in order"
        GameType.DARTZEE.getDescription("ZZZZ") shouldBe "Dartzee"
    }

    @Test
    fun `Dartzee description with valid template`() {
        val t = insertDartzeeTemplate(name = "Goomba")
        GameType.DARTZEE.getDescription(t.rowId) shouldBe "Dartzee - Goomba"
    }
}
