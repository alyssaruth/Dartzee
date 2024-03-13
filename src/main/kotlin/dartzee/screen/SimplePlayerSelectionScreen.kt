package dartzee.screen

import dartzee.bean.GameSetupPlayerSelector
import dartzee.core.util.setFontSize
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.game.X01_PARTY_CONFIG
import dartzee.utils.InjectedThings.gameLauncher
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

class SimplePlayerSelectionScreen : EmbeddedScreen() {
    private val lblTitle = JLabel()
    private val playerSelector = GameSetupPlayerSelector()

    init {
        lblTitle.setFontSize(32)
        lblTitle.border = EmptyBorder(40, 10, 10, 10)
        lblTitle.horizontalAlignment = JLabel.CENTER
        lblTitle.text = "Select Players"
        add(lblTitle, BorderLayout.NORTH)
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
                X01_PARTY_CONFIG.toJson(),
                playerSelector.pairMode(),
            )

        gameLauncher.launchNewGame(launchParams)
    }

    override fun getNextText() = "Launch Game"

    override fun showNextButton() = true

    override fun getScreenName() = "Select Players"
}
