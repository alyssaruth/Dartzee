package dartzee.screen

import dartzee.`object`.GameLauncher
import dartzee.bean.*
import dartzee.core.bean.items
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertPlayer
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldNotContain
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
        val screen = GameSetupScreen()
        screen.initialise()

        //Default - single game
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Game"

        //First to
        screen.rdbtnFirstTo.doClick()
        screen.lblWins.isVisible shouldBe true
        screen.spinnerWins.isVisible shouldBe true
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Match"

        //Points-based
        screen.rdbtnPoints.doClick()
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe true
        screen.spinnerGames.isVisible shouldBe true
        screen.matchConfigPanel.components.toList() shouldContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Match"

        //Back to single game
        screen.rdbtnSingleGame.doClick()
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Game"
    }

    @Test
    fun `Should update based on whether a Dartzee template is selected`()
    {
        insertDartzeeTemplate(name = "Template")

        val screen = GameSetupScreen()
        screen.gameTypeComboBox.updateSelection(GameType.DARTZEE)

        screen.btnLaunch.isVisible shouldBe false
        screen.btnNext.isVisible shouldBe true

        val dartzeeParamPanel = screen.gameParamFilterPanel as GameParamFilterPanelDartzee
        dartzeeParamPanel.comboBox.selectedIndex = 2

        screen.btnLaunch.isVisible shouldBe true
        screen.btnNext.isVisible shouldBe false
    }

    @Test
    fun `Should retrieve Dartzee rules when launching a Dartzee game from a template`()
    {

    }

    @Test
    fun `Should launch a first-to match with the right parameters`()
    {

    }

    @Test
    fun `Should launch a points based match with the right parameters`()
    {

    }

    @Test
    fun `Should perform validation on Dartzee mode when trying to hit Next`()
    {

    }

    @Test
    fun `Should switch to the DartzeeRuleSetupScreen on Next, passing through the right parameters`()
    {

    }

    private fun ComboBoxGameType.updateSelection(type: GameType)
    {
        selectedItem = items().find { it.hiddenData == type }
    }
}