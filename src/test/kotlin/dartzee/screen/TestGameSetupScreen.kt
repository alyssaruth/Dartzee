package dartzee.screen

import dartzee.`object`.GameLauncher
import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelX01
import dartzee.bean.getAllPlayers
import dartzee.core.bean.items
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestGameSetupScreen: AbstractTest()
{
    private val gameLauncher = mockk<GameLauncher>(relaxed = true)

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        InjectedThings.gameLauncher = gameLauncher
    }

    @Test
    fun `Should respond to changing game type`()
    {
        val screen = GameSetupScreen()
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelX01>()

        screen.gameTypeComboBox.updateSelection(GameType.GOLF)
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelGolf>()
    }

    @Test
    fun `Should perform player selector validation when attempting to launch a game`()
    {
        val screen = GameSetupScreen()
        screen.btnLaunch.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must select at least 1 player.")
    }

    @Test
    fun `Should initialise the player selector with players from the DB`()
    {
        insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()

        val players = screen.playerSelector.tablePlayersToSelectFrom.getAllPlayers()
        players.map { it.name } shouldBe listOf("Alice", "Bob", "Clive")
    }

    @Test
    fun `Should launch a single game with the selected players and game params`()
    {
        val alice = insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()
        screen.playerSelector.init(listOf(alice, clive))

        val gameParamsPanel = screen.gameParamFilterPanel as GameParamFilterPanelX01
        gameParamsPanel.spinner.value = 701

        screen.btnLaunch.doClick()
        verify { gameLauncher.launchNewGame(listOf(alice, clive), GameType.X01, "701", null) }
    }

    @Test
    fun `Should toggle the right components when switching between match types`()
    {
        val alice = insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()
        screen.playerSelector.init(listOf(alice, clive))

        val gameParamsPanel = screen.gameParamFilterPanel as GameParamFilterPanelX01
        gameParamsPanel.spinner.value = 701

        screen.btnLaunch.doClick()
        verify { gameLauncher.launchNewGame(listOf(alice, clive), GameType.X01, "701", null) }
    }

    private fun ComboBoxGameType.updateSelection(type: GameType)
    {
        selectedItem = items().find { it.hiddenData == type }
    }
}