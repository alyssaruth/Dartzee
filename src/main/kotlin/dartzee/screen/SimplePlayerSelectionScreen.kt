package dartzee.screen

import dartzee.bean.GameSetupPlayerSelector
import dartzee.game.FinishType
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.utils.InjectedThings.gameLauncher
import java.awt.BorderLayout

class SimplePlayerSelectionScreen : EmbeddedScreen() {
    private val playerSelector = GameSetupPlayerSelector()

    init {
        add(playerSelector, BorderLayout.CENTER)
    }

    override fun initialise() {
        playerSelector.init()
    }

    override fun nextPressed() {
        if (!playerSelector.valid(false)) {
            return
        }

        val launchParams =
            GameLaunchParams(
                playerSelector.getSelectedPlayers(),
                GameType.X01,
                X01Config(301, FinishType.Any).toJson(),
                playerSelector.pairMode(),
            )

        gameLauncher.launchNewGame(launchParams)
    }

    override fun getNextText() = "Launch Game"

    override fun showNextButton() = true

    override fun getScreenName() = "Select Players"
}
