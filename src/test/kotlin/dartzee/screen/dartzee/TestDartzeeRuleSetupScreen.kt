package dartzee.screen.dartzee

import com.github.alexburlton.swingtest.getChild
import dartzee.game.GameLauncher
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.db.DartsMatchEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartzeeRuleDto
import dartzee.screen.GameSetupScreen
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestDartzeeRuleSetupScreen: AbstractTest()
{
    @Test
    fun `Should have the right title n stuff`()
    {
        val scrn = makeDartzeeRuleSetupScreen()
        scrn.getScreenName() shouldBe "Dartzee - Custom Setup"
        scrn.getBackTarget().shouldBeInstanceOf<GameSetupScreen>()
        scrn.showNextButton() shouldBe true
    }

    @Test
    fun `Should update the next button text based on whether there is a match or not`()
    {
        val scrn = makeDartzeeRuleSetupScreen(match = null)
        scrn.getNextText() shouldBe "Launch Game"

        val scrnWithMatch = makeDartzeeRuleSetupScreen(match = insertDartsMatch())
        scrnWithMatch.getNextText() shouldBe "Launch Match"
    }

    @Test
    fun `Should launch a match`()
    {
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val match = insertDartsMatch()
        val players = listOf(insertPlayer(), insertPlayer())

        val scrn = makeDartzeeRuleSetupScreen(players, match = match)
        scrn.btnNext.doClick()

        verify { launcher.launchNewMatch(match, any())}
    }

    @Test
    fun `Should launch a single game and pass pairMode`()
    {
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val players = listOf(insertPlayer(), insertPlayer())
        val rules = listOf(makeDartzeeRuleDto(DartzeeDartRuleOdd()), makeDartzeeRuleDto(DartzeeDartRuleEven()))

        val scrn = makeDartzeeRuleSetupScreen(players, true)
        val panel = scrn.getChild<DartzeeRuleSetupPanel>()
        panel.addRulesToTable(rules)

        scrn.btnNext.doClick()

        val expectedParams = GameLaunchParams(players, GameType.DARTZEE, "", true, rules)
        verify { launcher.launchNewGame(expectedParams) }
    }

    private fun makeDartzeeRuleSetupScreen(
        players: List<PlayerEntity> = listOf(insertPlayer(), insertPlayer()),
        pairMode: Boolean = false,
        match: DartsMatchEntity? = null
    ) = DartzeeRuleSetupScreen(match, players, pairMode)
}