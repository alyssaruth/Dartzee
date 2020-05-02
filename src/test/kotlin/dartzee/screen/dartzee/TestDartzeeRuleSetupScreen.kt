package dartzee.screen.dartzee

import dartzee.`object`.GameLauncher
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.db.DartsMatchEntity
import dartzee.findComponent
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
import org.junit.Test

class TestDartzeeRuleSetupScreen: AbstractTest()
{
    @Test
    fun `Should have the right title n stuff`()
    {
        val scrn = DartzeeRuleSetupScreen()
        scrn.getScreenName() shouldBe "Dartzee - Custom Setup"
        scrn.getBackTarget().shouldBeInstanceOf<GameSetupScreen>()
        scrn.showNextButton() shouldBe true
    }

    @Test
    fun `Should update the next button text based on whether there is a match or not`()
    {
        val scrn = DartzeeRuleSetupScreen()

        scrn.setState(null, listOf())
        scrn.btnNext.text shouldBe "Launch Game >"

        scrn.setState(DartsMatchEntity(), listOf())
        scrn.btnNext.text shouldBe "Launch Match >"
    }

    @Test
    fun `Should launch a match`()
    {
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val match = insertDartsMatch()
        val players = listOf(insertPlayer(), insertPlayer())

        val scrn = DartzeeRuleSetupScreen()
        scrn.setState(match, players)
        scrn.players shouldBe players

        scrn.btnNext.doClick()

        verify { launcher.launchNewMatch(match, listOf())}
    }

    @Test
    fun `Should launch a single game`()
    {
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val players = listOf(insertPlayer(), insertPlayer())
        val rules = listOf(makeDartzeeRuleDto(DartzeeDartRuleOdd()), makeDartzeeRuleDto(DartzeeDartRuleEven()))

        val scrn = DartzeeRuleSetupScreen()
        scrn.setState(null, players)
        val panel = scrn.findComponent<DartzeeRuleSetupPanel>()
        panel.addRulesToTable(rules)

        scrn.btnNext.doClick()

        verify { launcher.launchNewGame(players, GameType.DARTZEE, "", rules) }
    }
}