package burlton.dartzee.test.utils

import burlton.dartzee.code.bean.GameParamFilterPanelDartzee
import burlton.dartzee.code.bean.GameParamFilterPanelGolf
import burlton.dartzee.code.bean.GameParamFilterPanelRoundTheClock
import burlton.dartzee.code.bean.GameParamFilterPanelX01
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.doesHighestWin
import burlton.dartzee.code.utils.getFilterPanel
import burlton.dartzee.code.utils.getTypeDesc
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameUtil: AbstractDartsTest()
{
    @Test
    fun `Sensible descriptions when no params`()
    {
        getTypeDesc(GAME_TYPE_X01) shouldBe "X01"
        getTypeDesc(GAME_TYPE_GOLF) shouldBe "Golf"
        getTypeDesc(GAME_TYPE_ROUND_THE_CLOCK) shouldBe "Round the Clock"
        getTypeDesc(GAME_TYPE_DARTZEE) shouldBe "Dartzee"
        getTypeDesc(-1) shouldBe "<Game Type>"
    }

    @Test
    fun `Filter panel mappings`()
    {
        getFilterPanel(GAME_TYPE_X01).shouldBeInstanceOf<GameParamFilterPanelX01>()
        getFilterPanel(GAME_TYPE_GOLF).shouldBeInstanceOf<GameParamFilterPanelGolf>()
        getFilterPanel(GAME_TYPE_ROUND_THE_CLOCK).shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
        getFilterPanel(GAME_TYPE_DARTZEE).shouldBeInstanceOf<GameParamFilterPanelDartzee>()
    }

    @Test
    fun `Does highest win`()
    {
        doesHighestWin(GAME_TYPE_X01) shouldBe false
        doesHighestWin(GAME_TYPE_GOLF) shouldBe false
        doesHighestWin(GAME_TYPE_ROUND_THE_CLOCK) shouldBe false
        doesHighestWin(GAME_TYPE_DARTZEE) shouldBe true
    }
}