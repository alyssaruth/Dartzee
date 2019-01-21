package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.ComboBoxGameType
import burlton.dartzee.code.bean.GameParamFilterPanel
import burlton.dartzee.code.bean.GameParamFilterPanelX01
import burlton.dartzee.code.bean.PlayerSelector
import burlton.dartzee.code.db.getFilterPanel
import burlton.dartzee.code.screen.game.DartsGameScreen
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.TitledBorder

class GameSetupScreen : EmbeddedScreen()
{
    private val panelGameType = JPanel()
    private val panelPlayers = JPanel()
    private val launchPanel = JPanel()
    private val btnLaunch = JButton("Launch Game")
    private val playerSelector = PlayerSelector()
    private val gameTypeComboBox = ComboBoxGameType()
    private val panel = JPanel()
    private var gameParamFilterPanel: GameParamFilterPanel? = GameParamFilterPanelX01()

    init
    {
        panelGameType.border = TitledBorder(null, "Game Type", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        add(panelGameType, BorderLayout.NORTH)
        panelGameType.layout = GridLayout(0, 1, 0, 0)

        panelGameType.add(panel)
        panel.add(gameTypeComboBox)

        panelGameType.add(gameParamFilterPanel)

        panelPlayers.border = TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        add(panelPlayers, BorderLayout.CENTER)
        panelPlayers.layout = BorderLayout(0, 0)
        panelPlayers.add(launchPanel, BorderLayout.SOUTH)
        launchPanel.add(btnLaunch)
        panelPlayers.add(playerSelector, BorderLayout.CENTER)

        gameTypeComboBox.addActionListener(this)

        btnLaunch.addActionListener(this)
    }

    override fun initialise()
    {
        playerSelector.init()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (arg0.source === btnLaunch)
        {
            launchGame()
        }
        else if (arg0.source === gameTypeComboBox)
        {
            //Remove what's already there, if applicable
            gameParamFilterPanel?.let{
                panelGameType.remove(gameParamFilterPanel)
            }

            gameParamFilterPanel = getFilterPanel(gameTypeComboBox.gameType)

            //We may not have one, e.g. for Dartzee
            if (gameParamFilterPanel != null)
            {
                panelGameType.add(gameParamFilterPanel)
            }

            panelGameType.revalidate()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    private fun getGameParams(): String
    {
        return gameParamFilterPanel?.getGameParams() ?: ""
    }

    fun launchGame()
    {
        if (playerSelector.valid())
        {
            val selectedPlayers = playerSelector.selectedPlayers
            DartsGameScreen.launchNewGame(selectedPlayers, gameTypeComboBox.gameType, getGameParams())
        }
    }

    override fun nextPressed()
    {
        val scrn = ScreenCache.getScreen(MatchSetupScreen::class.java)
        scrn!!.init(playerSelector.selectedPlayers, gameTypeComboBox.gameType, getGameParams())
        ScreenCache.switchScreen(scrn)
    }

    override fun getScreenName(): String
    {
        return "Game Setup"
    }

    override fun showNextButton(): Boolean
    {
        return true
    }

    override fun getNextText(): String
    {
        return "Match Setup"
    }
}
