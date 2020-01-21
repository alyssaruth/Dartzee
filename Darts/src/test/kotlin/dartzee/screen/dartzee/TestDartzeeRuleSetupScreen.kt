package dartzee.test.screen.dartzee

import dartzee.db.DartsMatchEntity
import dartzee.screen.GameSetupScreen
import dartzee.screen.dartzee.DartzeeRuleSetupScreen
import dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
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

    //TODO - Make GameLauncher injected so we can test the launch behaviour
}