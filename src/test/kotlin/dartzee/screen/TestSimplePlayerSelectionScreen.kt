package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.GameSetupPlayerSelector
import dartzee.core.helper.verifyNotCalled
import dartzee.game.FinishType
import dartzee.game.GameLaunchParams
import dartzee.game.GameLauncher
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.preparePlayers
import dartzee.utils.InjectedThings
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import javax.swing.JButton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestSimplePlayerSelectionScreen : AbstractTest() {
    private val gameLauncher = mockk<GameLauncher>(relaxed = true)

    @BeforeEach
    fun beforeEach() {
        InjectedThings.gameLauncher = gameLauncher
    }

    @Test
    fun `Should perform player selector validation when attempting to launch a game`() {
        val screen = SimplePlayerSelectionScreen()
        screen.postInit()
        screen.clickChild<JButton>(text = "Launch Game >", async = true)

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "You must select at least 1 player."
        error.clickOk()
        verifyNotCalled { gameLauncher.launchNewGame(any()) }
    }

    @Test
    fun `Should launch a game of 301 with the right parameters`() {
        val (p1, p2) = preparePlayers(2)

        val screen = SimplePlayerSelectionScreen()
        screen.initialise()
        screen.postInit()

        screen.getChild<GameSetupPlayerSelector>().init(listOf(p1, p2))
        screen.clickChild<JButton>(text = "Launch Game >")

        verify {
            gameLauncher.launchNewGame(
                GameLaunchParams(
                    listOf(p1, p2),
                    GameType.X01,
                    X01Config(301, FinishType.Any).toJson(),
                    false
                )
            )
        }
    }
}
