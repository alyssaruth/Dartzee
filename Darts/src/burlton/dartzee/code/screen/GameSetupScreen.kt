package burlton.dartzee.code.screen

import burlton.core.code.util.Debug
import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.bean.ComboBoxGameType
import burlton.dartzee.code.bean.GameParamFilterPanel
import burlton.dartzee.code.bean.GameParamFilterPanelX01
import burlton.dartzee.code.bean.PlayerSelector
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.desktopcore.code.bean.RadioButtonPanel
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.TitledBorder

class GameSetupScreen : EmbeddedScreen()
{
    private val panelSetup = JPanel()
    private val panelGameType = JPanel()
    private val panelPlayers = JPanel()
    private val launchPanel = JPanel()
    private val btnLaunch = JButton("Launch Game")
    private val playerSelector = PlayerSelector()
    private val gameTypeComboBox = ComboBoxGameType()
    private val panelGameTypeCb = JPanel()
    private var gameParamFilterPanel: GameParamFilterPanel? = GameParamFilterPanelX01()

    private val panel = RadioButtonPanel()
    private val rdbtnSingleGame = JRadioButton("Single Game")
    private val rdbtnFirstTo = JRadioButton("First to")
    private val rdbtnPoints = JRadioButton("Points-based")
    private val spinnerWins = JSpinner()
    private val spinnerGames = JSpinner()
    private val lblWins = JLabel("  wins")
    private val lblGames = JLabel("  games  ")
    private val spinnerPoints1st = JSpinner()
    private val lblst = JLabel("1st")
    private val spinnerPoints2nd = JSpinner()
    private val lb2nd = JLabel("2nd")
    private val spinnerPoints3rd = JSpinner()
    private val lb3rd = JLabel("3rd")
    private val spinnerPoints4th = JSpinner()
    private val lb4th = JLabel("4th")
    private val panelPointBreakdown = JPanel()

    init
    {
        panelSetup.layout = BorderLayout(0, 0)

        panelGameType.border = TitledBorder(null, "Game Type", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelSetup.add(panelGameType, BorderLayout.NORTH)
        panelGameType.layout = GridLayout(0, 1, 0, 0)

        add(panelSetup, BorderLayout.NORTH)

        panelGameType.add(panelGameTypeCb)
        panelGameTypeCb.add(gameTypeComboBox)

        panelGameType.add(gameParamFilterPanel)

        panelPlayers.border = TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        add(panelPlayers, BorderLayout.CENTER)
        panelPlayers.layout = BorderLayout(0, 0)
        panelPlayers.add(launchPanel, BorderLayout.SOUTH)
        launchPanel.add(btnLaunch)
        panelPlayers.add(playerSelector, BorderLayout.CENTER)

        panelSetup.add(panel, BorderLayout.CENTER)

        panel.border = TitledBorder(null, "Match Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panel.layout = MigLayout("", "[80px][101px][grow][45px][][][]", "[][]")

        panel.add(rdbtnSingleGame, "flowy,cell 0 0,alignx left,aligny top")
        panel.add(rdbtnFirstTo, "flowy,cell 0 1,alignx left,aligny top")

        panel.add(spinnerWins, "flowx,cell 1 1,alignx left,aligny center")
        spinnerWins.model = SpinnerNumberModel(2, 2, 15, 1)
        panel.add(rdbtnPoints, "cell 0 2,alignx left,aligny top")
        spinnerGames.model = SpinnerNumberModel(4, 2, 15, 1)

        panel.add(spinnerGames, "flowx,cell 1 2,alignx left,aligny top")

        panel.add(lblWins, "cell 1 1")

        panel.add(lblGames, "cell 1 2,alignx left,aligny top")


        panelPointBreakdown.layout = MigLayout("", "[]", "[]")

        panelPointBreakdown.add(lblst, "flowy,cell 0 0,alignx center")
        spinnerPoints1st.model = SpinnerNumberModel(4, 0, 20, 1)
        panelPointBreakdown.add(spinnerPoints1st, "cell 0 0,alignx center")

        panelPointBreakdown.add(lb2nd, "flowy,cell 1 0,alignx center")
        spinnerPoints2nd.model = SpinnerNumberModel(3, 0, 20, 1)
        panelPointBreakdown.add(spinnerPoints2nd, "cell 1 0,alignx center")

        panelPointBreakdown.add(lb3rd, "flowy,cell 2 0,alignx center")
        spinnerPoints3rd.model = SpinnerNumberModel(2, 0, 20, 1)
        panelPointBreakdown.add(spinnerPoints3rd, "cell 2 0,alignx center")

        panelPointBreakdown.add(lb4th, "flowy,cell 3 0,alignx center")
        spinnerPoints4th.model = SpinnerNumberModel(1, 0, 20, 1)
        panelPointBreakdown.add(spinnerPoints4th, "cell 3 0,alignx center")

        panel.addActionListener(this)
        gameTypeComboBox.addActionListener(this)

        btnLaunch.addActionListener(this)
    }

    override fun initialise()
    {
        playerSelector.init()

        toggleComponents()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (arg0.source === btnLaunch)
        {
            launchGame()
        }
        else if (panel.isEventSource(arg0))
        {
            toggleComponents()
        }
        else if (arg0.source === gameTypeComboBox)
        {
            //Remove what's already there, if applicable
            gameParamFilterPanel?.let{
                panelGameType.remove(gameParamFilterPanel)
            }

            gameParamFilterPanel = GameEntity.getFilterPanel(gameTypeComboBox.gameType)

            //We may not have one, e.g. for Dartzee
            if (gameParamFilterPanel != null)
            {
                panelGameType.add(gameParamFilterPanel)
            }

            panelGameType.revalidate()

            toggleComponents()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    private fun toggleComponents()
    {
        if (rdbtnSingleGame.isSelected)
        {
            btnLaunch.text = "Launch Game"
        }
        else
        {
            btnLaunch.text = "Launch Match"
        }

        spinnerWins.isVisible = rdbtnFirstTo.isSelected
        lblWins.isVisible = rdbtnFirstTo.isSelected

        spinnerGames.isVisible = rdbtnPoints.isSelected
        lblGames.isVisible = rdbtnPoints.isSelected
        lblst.isVisible = rdbtnPoints.isSelected
        lb2nd.isVisible = rdbtnPoints.isSelected
        lb3rd.isVisible = rdbtnPoints.isSelected
        lb4th.isVisible = rdbtnPoints.isSelected
        spinnerPoints1st.isVisible = rdbtnPoints.isSelected
        spinnerPoints2nd.isVisible = rdbtnPoints.isSelected
        spinnerPoints3rd.isVisible = rdbtnPoints.isSelected
        spinnerPoints4th.isVisible = rdbtnPoints.isSelected

        if (rdbtnPoints.isSelected)
        {
            panel.add(panelPointBreakdown, "cell 0 3,span")
        }
        else
        {
            panel.remove(panelPointBreakdown)
        }

        val dartzee = gameTypeComboBox.gameType == GAME_TYPE_DARTZEE
        btnLaunch.isVisible = !dartzee
        toggleNextVisibility(dartzee)

        invalidate()
        revalidate()
        validate()
    }

    private fun getGameParams(): String
    {
        return gameParamFilterPanel?.getGameParams() ?: ""
    }

    private fun launchGame()
    {
        if (!playerSelector.valid())
        {
            return
        }

        val match = factoryMatch()
        val selectedPlayers = playerSelector.selectedPlayers

        if (match == null)
        {
            DartsGameScreen.launchNewGame(selectedPlayers, gameTypeComboBox.gameType, getGameParams())
        }
        else
        {
            match.players = selectedPlayers
            match.gameType = gameTypeComboBox.gameType
            match.gameParams = getGameParams()

            DartsGameScreen.launchNewMatch(match)
        }
    }

    private fun factoryMatch(): DartsMatchEntity?
    {
        if (rdbtnFirstTo.isSelected)
        {
            val games = spinnerWins.value as Int
            return DartsMatchEntity.factoryFirstTo(games)
        }
        else if (rdbtnPoints.isSelected)
        {
            val games = spinnerGames.value as Int
            return DartsMatchEntity.factoryPoints(games, getPointsXml())
        }
        else
        {
            return null
        }
    }

    private fun getPointsXml(): String
    {
        val doc = XmlUtil.factoryNewDocument()
        val rootElement = doc!!.createElement("MatchParams")
        rootElement.setAttribute("First", "" + spinnerPoints1st.value as Int)
        rootElement.setAttribute("Second", "" + spinnerPoints2nd.value as Int)
        rootElement.setAttribute("Third", "" + spinnerPoints3rd.value as Int)
        rootElement.setAttribute("Fourth", "" + spinnerPoints4th.value as Int)

        doc.appendChild(rootElement)
        return XmlUtil.getStringFromDocument(doc)
    }

    override fun getScreenName(): String
    {
        return "Game Setup"
    }

    override fun nextPressed()
    {
        if (gameTypeComboBox.gameType == GAME_TYPE_DARTZEE)
        {
            if (!playerSelector.valid())
            {
                return
            }

            val match = factoryMatch()
            val selectedPlayers = playerSelector.selectedPlayers

            val scrn = ScreenCache.getScreen(DartzeeRuleSetupScreen::class.java)
            scrn.setState(match, selectedPlayers)
            ScreenCache.switchScreen(scrn)
        }
        else
        {
            Debug.stackTrace("Unexpected screen state. GameType = ${gameTypeComboBox.gameType}")
        }
    }
}
