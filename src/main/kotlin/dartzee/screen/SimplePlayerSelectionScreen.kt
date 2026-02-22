package dartzee.screen

import dartzee.bean.GameSetupPlayerSelector
import dartzee.core.bean.makeTransparentTextPane
import dartzee.core.util.alignCentrally
import dartzee.core.util.append
import dartzee.core.util.setFontSize
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.game.X01_PARTY_CONFIG
import dartzee.theme.themedIcon
import dartzee.utils.InjectedThings.gameLauncher
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class SimplePlayerSelectionScreen : EmbeddedScreen() {
    private val lblTitle = JLabel()
    private val playerSelector = GameSetupPlayerSelector()

    init {
        lblTitle.setFontSize(32)
        lblTitle.border = EmptyBorder(40, 10, 10, 10)
        lblTitle.horizontalAlignment = JLabel.CENTER
        lblTitle.text = "Game Setup"

        val panelNorth = JPanel()
        panelNorth.layout = BorderLayout(0, 0)
        add(panelNorth, BorderLayout.NORTH)
        panelNorth.add(lblTitle, BorderLayout.NORTH)

        val textPane =
            makeTransparentTextPane().apply {
                alignCentrally()
                setFontSize(18)
                border = EmptyBorder(0, 0, 25, 0)

                append("Select who is playing using the controls below. Play in pairs using the ")
                val lbl = JLabel(themedIcon("/buttons/teams.png"))
                lbl.alignmentY = 0.75f
                insertComponent(lbl)
                append(" button.")
                append("\n")
                append("Change the order players throw in using the other buttons.")
            }
        panelNorth.add(textPane, BorderLayout.CENTER)

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

    override fun getScreenName() = "Game Setup"
}
