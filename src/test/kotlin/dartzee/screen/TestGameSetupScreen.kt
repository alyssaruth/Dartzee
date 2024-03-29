package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelX01
import dartzee.bean.SpinnerX01
import dartzee.bean.getAllPlayers
import dartzee.core.bean.items
import dartzee.core.helper.verifyNotCalled
import dartzee.dartzee.aggregate.DartzeeTotalRulePrime
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.db.DartsMatchEntity
import dartzee.db.EntityName
import dartzee.db.PlayerEntity
import dartzee.game.FinishType
import dartzee.game.GameLaunchParams
import dartzee.game.GameLauncher
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.game.X01Config
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartzeeRuleDto
import dartzee.launchParamsEqual
import dartzee.screen.dartzee.DartzeeRuleSetupScreen
import dartzee.updateSelection
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import javax.swing.JButton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestGameSetupScreen : AbstractTest() {
    private val gameLauncher = mockk<GameLauncher>(relaxed = true)

    @BeforeEach
    fun beforeEach() {
        InjectedThings.gameLauncher = gameLauncher
    }

    @Test
    fun `Should respond to changing game type`() {
        val screen = GameSetupScreen()
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelX01>()

        screen.gameTypeComboBox.updateSelection(GameType.GOLF)
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelGolf>()
    }

    @Test
    fun `Should perform player selector validation when attempting to launch a game`() {
        val screen = GameSetupScreen()
        screen.clickChild<JButton>(text = "Launch Game", async = true)

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "You must select at least 1 player."
        error.clickOk()
        verifyNotCalled { gameLauncher.launchNewGame(any()) }
    }

    @Test
    fun `Should initialise the player selector with players from the DB`() {
        insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()

        val players = screen.playerSelector.tablePlayersToSelectFrom.getAllPlayers()
        players.map { it.name } shouldBe listOf("Alice", "Bob", "Clive")
    }

    @Test
    fun `Should launch a single game with the selected players and game params`() {
        val alice = insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()
        screen.playerSelector.init(listOf(alice, clive))

        val gameParamsPanel = screen.gameParamFilterPanel as GameParamFilterPanelX01
        gameParamsPanel.getChild<SpinnerX01>().value = 701

        screen.launchButton().doClick()

        val expectedParams =
            GameLaunchParams(
                listOf(alice, clive),
                GameType.X01,
                X01Config(701, FinishType.Doubles).toJson(),
                false,
                null
            )
        verify { gameLauncher.launchNewGame(expectedParams) }
    }

    @Test
    fun `Should toggle the right components when switching between match types`() {
        val screen = GameSetupScreen()
        screen.initialise()

        // Default - single game
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.launchButton().text shouldBe "Launch Game"

        // First to
        screen.rdbtnFirstTo.doClick()
        screen.lblWins.isVisible shouldBe true
        screen.spinnerWins.isVisible shouldBe true
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.launchButton().text shouldBe "Launch Match"

        // Points-based
        screen.rdbtnPoints.doClick()
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe true
        screen.spinnerGames.isVisible shouldBe true
        screen.matchConfigPanel.components.toList() shouldContain screen.panelPointBreakdown
        screen.launchButton().text shouldBe "Launch Match"

        // Back to single game
        screen.rdbtnSingleGame.doClick()
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.launchButton().text shouldBe "Launch Game"
    }

    @Test
    fun `Should update based on whether a Dartzee template is selected`() {
        insertDartzeeTemplate(name = "Template")

        val screen = GameSetupScreen()
        screen.gameTypeComboBox.updateSelection(GameType.DARTZEE)

        screen.launchButton().isVisible shouldBe false
        screen.btnNext.isVisible shouldBe true

        val dartzeeParamPanel = screen.gameParamFilterPanel as GameParamFilterPanelDartzee
        dartzeeParamPanel.comboBox.selectedIndex = 2

        screen.launchButton().isVisible shouldBe true
        screen.btnNext.isVisible shouldBe false
    }

    @Test
    fun `Should retrieve Dartzee rules when launching a Dartzee game from a template`() {
        val templateId = insertDartzeeTemplate().rowId

        val ruleOne =
            makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleOdd(), DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRulePrime())

        ruleOne.toEntity(1, EntityName.DartzeeTemplate, templateId).saveToDatabase()
        ruleTwo.toEntity(2, EntityName.DartzeeTemplate, templateId).saveToDatabase()

        val (screen, players) = makeGameSetupScreenReadyToLaunch()
        screen.gameTypeComboBox.updateSelection(GameType.DARTZEE)

        val dartzeeParamPanel = screen.gameParamFilterPanel as GameParamFilterPanelDartzee
        dartzeeParamPanel.comboBox.selectedIndex = 2

        screen.launchButton().doClick()

        val expectedParams =
            GameLaunchParams(players, GameType.DARTZEE, templateId, false, listOf(ruleOne, ruleTwo))
        verify { gameLauncher.launchNewGame(launchParamsEqual(expectedParams)) }
    }

    @Test
    fun `Should launch a first-to match with the right parameters`() {
        val slot = slot<DartsMatchEntity>()
        every { gameLauncher.launchNewMatch(capture(slot), any()) } just runs

        val (scrn, players) = makeGameSetupScreenReadyToLaunch()

        scrn.rdbtnFirstTo.doClick()
        scrn.spinnerWins.value = 7

        scrn.launchButton().doClick()

        val launchParams =
            GameLaunchParams(players, GameType.X01, DEFAULT_X01_CONFIG.toJson(), false)
        verify { gameLauncher.launchNewMatch(any(), launchParams) }

        val match = slot.captured
        match.mode shouldBe MatchMode.FIRST_TO
        match.games shouldBe 7
        match.matchParams shouldBe ""
    }

    @Test
    fun `Should launch a points based match with the right parameters`() {
        val slot = slot<DartsMatchEntity>()
        every { gameLauncher.launchNewMatch(capture(slot), any()) } just runs

        val (scrn, _) = makeGameSetupScreenReadyToLaunch()
        scrn.gameTypeComboBox.updateSelection(GameType.GOLF)

        scrn.rdbtnPoints.doClick()
        scrn.spinnerGames.value = 8
        scrn.spinners[0].value = 15
        scrn.spinners[1].value = 9
        scrn.spinners[2].value = 6
        scrn.spinners[3].value = 3
        scrn.spinners[4].value = 2
        scrn.spinners[5].value = 1

        scrn.launchButton().doClick()

        verify { gameLauncher.launchNewMatch(any(), any()) }

        val match = slot.captured
        match.mode shouldBe MatchMode.POINTS
        match.games shouldBe 8
        match.matchParams shouldBe DartsMatchEntity.constructPointsJson(15, 9, 6, 3, 2, 1)
    }

    @Test
    fun `Should perform validation on Dartzee mode when trying to hit Next`() {
        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf())

        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)
        setupScreen.clickChild<JButton>(text = "Next > ", async = true)

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "You must select at least 1 player."
        error.clickOk()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldNotBeInstanceOf<DartzeeRuleSetupScreen>()
    }

    @Test
    fun `Should switch to the DartzeeRuleSetupScreen on Next, passing through the right parameters`() {
        val p1 = insertPlayer(strategy = "")
        val p2 = insertPlayer(strategy = "")

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)
        setupScreen.btnNext.doClick()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<DartzeeRuleSetupScreen>()
        currentScreen.btnNext.text shouldBe "Launch Game >"
        currentScreen.nextPressed()

        val expectedParams =
            GameLaunchParams(listOf(p1, p2), GameType.DARTZEE, "", false, emptyList())
        verify { gameLauncher.launchNewGame(expectedParams) }
    }

    @Test
    fun `Should switch to the DartzeeRuleSetupScreen for a match`() {
        val slot = slot<DartsMatchEntity>()
        every { gameLauncher.launchNewMatch(capture(slot), any()) } just runs

        val p1 = insertPlayer(strategy = "")
        val p2 = insertPlayer(strategy = "")

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        setupScreen.rdbtnFirstTo.doClick()
        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)
        setupScreen.btnNext.doClick()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<DartzeeRuleSetupScreen>()
        currentScreen.btnNext.text shouldBe "Launch Match >"
        currentScreen.nextPressed()

        val expectedParams =
            GameLaunchParams(listOf(p1, p2), GameType.DARTZEE, "", false, emptyList())
        verify { gameLauncher.launchNewMatch(any(), expectedParams) }

        val match = slot.captured
        match.games shouldBe 2
        match.mode shouldBe MatchMode.FIRST_TO
    }

    @Test
    fun `Should update the game parameters panel on initialise`() {
        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)

        val originalFilterPanel = setupScreen.gameParamFilterPanel as GameParamFilterPanelDartzee
        originalFilterPanel.comboBox.items().mapNotNull { it.hiddenData }.shouldBeEmpty()

        insertDartzeeTemplate(name = "New Template")
        setupScreen.initialise()
        val newFilterPanel = setupScreen.gameParamFilterPanel as GameParamFilterPanelDartzee
        newFilterPanel.comboBox.items().mapNotNull { it.hiddenData }.shouldHaveSize(1)
    }

    private fun GameSetupScreen.launchButton() = getChild<JButton>("LaunchButton")

    private fun makeGameSetupScreenReadyToLaunch(): Pair<GameSetupScreen, List<PlayerEntity>> {
        val p1 = insertPlayer(strategy = "")
        val p2 = insertPlayer(strategy = "")

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        return setupScreen to listOf(p1, p2)
    }
}
