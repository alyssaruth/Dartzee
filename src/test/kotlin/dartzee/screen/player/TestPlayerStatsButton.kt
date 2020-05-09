package dartzee.screen.player

import dartzee.doHover
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerStatisticsScreen
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test

class TestPlayerStatsButton: AbstractTest()
{
    @Test
    fun `Should have the correct text based on values passed in`()
    {
        val player = insertPlayer()

        val button = PlayerStatsButton(player, GameType.X01, 10, 35)
        button.text shouldBe "<html><center><h3>X01</h3> <b>Played: </b> 10<br><b>Best game: </b> 35</center></html>"
        button.isEnabled shouldBe true

        val golfButton = PlayerStatsButton(player, GameType.GOLF, 25, 18)
        golfButton.text shouldBe "<html><center><h3>Golf</h3> <b>Played: </b> 25<br><b>Best game: </b> 18</center></html>"
        golfButton.isEnabled shouldBe true
    }

    @Test
    fun `Should be disabled if 0 games played, and not do anything on hover`()
    {
        val player = insertPlayer()

        val expectedText = "<html><center><h3>X01</h3> <b>Played: </b> 0<br><b>Best game: </b> -</center></html>"

        val button = PlayerStatsButton(player, GameType.X01, 0, 0)
        button.text shouldBe expectedText
        button.isEnabled shouldBe false

        button.doHover()
        button.text shouldBe expectedText
    }

    @Test
    fun `Should change text on hover if enabled`()
    {
        val player = insertPlayer()

        val button = PlayerStatsButton(player, GameType.X01, 10, 35)
        button.doHover()

        button.text shouldBe "<html><h3>X01 stats &gt;</h3></html>"
    }

    @Test
    fun `Should switch to the players stats on click`()
    {
        val player = insertPlayer()
        val button = PlayerStatsButton(player, GameType.X01, 10, 35)
        button.doClick()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<PlayerStatisticsScreen>()

        val statsScreen = currentScreen as PlayerStatisticsScreen
        statsScreen.player shouldBe player
        statsScreen.gameType shouldBe GameType.X01
    }
}