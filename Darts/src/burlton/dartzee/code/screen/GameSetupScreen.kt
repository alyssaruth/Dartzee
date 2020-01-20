package burlton.dartzee.code.screen

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.`object`.GameLauncher
import burlton.dartzee.code.bean.*
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.DartsMatchEntity.Companion.constructPointsXml
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.screen.dartzee.DartzeeRuleSetupScreen
import burlton.dartzee.code.utils.getFilterPanel
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
    private var gameParamFilterPanel: GameParamFilterPanel = GameParamFilterPanelX01()

    private val matchConfigPanel = RadioButtonPanel()
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
        panelSetup.add(matchConfigPanel, BorderLayout.CENTER)
        matchConfigPanel.border = TitledBorder(null, "Match Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        matchConfigPanel.layout = MigLayout("", "[80px][101px][grow][45px][][][]", "[][]")
        matchConfigPanel.add(rdbtnSingleGame, "flowy,cell 0 0,alignx left,aligny top")
        matchConfigPanel.add(rdbtnFirstTo, "flowy,cell 0 1,alignx left,aligny top")
        matchConfigPanel.add(spinnerWins, "flowx,cell 1 1,alignx left,aligny center")
        spinnerWins.model = SpinnerNumberModel(2, 2, 15, 1)
        matchConfigPanel.add(rdbtnPoints, "cell 0 2,alignx left,aligny top")
        spinnerGames.model = SpinnerNumberModel(4, 2, 15, 1)
        matchConfigPanel.add(spinnerGames, "flowx,cell 1 2,alignx left,aligny top")
        matchConfigPanel.add(lblWins, "cell 1 1")
        matchConfigPanel.add(lblGames, "cell 1 2,alignx left,aligny top")
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

        matchConfigPanel.addActionListener(this)
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
        else if (arg0.source === gameTypeComboBox)
        {
            //Remove what's already there
            gameParamFilterPanel.removeActionListener(this)
            panelGameType.remove(gameParamFilterPanel)

            gameParamFilterPanel = getFilterPanel(gameTypeComboBox.getGameType())
            panelGameType.add(gameParamFilterPanel)
            gameParamFilterPanel.addActionListener(this)

            panelGameType.revalidate()

            toggleComponents()
        }
        else if (arg0.source != btnNext && arg0.source != btnBack)
        {
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
            matchConfigPanel.add(panelPointBreakdown, "cell 0 3,span")
        }
        else
        {
            matchConfigPanel.remove(panelPointBreakdown)
        }

        val customDartzee = gameTypeComboBox.getGameType() == GAME_TYPE_DARTZEE && gameParamFilterPanel.getGameParams() == ""
        btnLaunch.isVisible = !customDartzee
        toggleNextVisibility(customDartzee)

        invalidate()
        revalidate()
        validate()
    }

    private fun getGameParams() = gameParamFilterPanel.getGameParams()

    private fun launchGame()
    {
        val match = factoryMatch()
        if (!playerSelector.valid(match != null, gameTypeComboBox.getGameType()))
        {
            return
        }

        val selectedPlayers = playerSelector.getSelectedPlayers()
        val rules = retrieveDartzeeRules()

        if (match == null)
        {
            GameLauncher.launchNewGame(selectedPlayers, gameTypeComboBox.getGameType(), getGameParams(), rules)
        }
        else
        {
            match.players = selectedPlayers
            match.gameType = gameTypeComboBox.getGameType()
            match.gameParams = getGameParams()

            GameLauncher.launchNewMatch(match, rules)
        }
    }

    private fun retrieveDartzeeRules(): List<DartzeeRuleDto>?
    {
        if (gameTypeComboBox.getGameType() != GAME_TYPE_DARTZEE)
        {
            return null
        }

        val dartzeeTemplate = (gameParamFilterPanel as GameParamFilterPanelDartzee).getSelectedTemplate()!!
        val rules = DartzeeRuleEntity().retrieveForTemplate(dartzeeTemplate.rowId)
        return rules.map { it.toDto() }
    }

    private fun factoryMatch(): DartsMatchEntity?
    {
        val games = spinnerWins.value as Int
        return when
        {
            rdbtnFirstTo.isSelected -> DartsMatchEntity.factoryFirstTo(games)
            rdbtnPoints.isSelected -> DartsMatchEntity.factoryPoints(games, getPointsXml())
            else -> null
        }
    }

    private fun getPointsXml(): String
    {
        return constructPointsXml(spinnerPoints1st.value as Int,
                spinnerPoints2nd.value as Int,
                spinnerPoints3rd.value as Int,
                spinnerPoints4th.value as Int)
    }

    override fun getScreenName() = "Game Setup"

    override fun nextPressed()
    {
        if (gameTypeComboBox.getGameType() == GAME_TYPE_DARTZEE)
        {
            val match = factoryMatch()
            if (!playerSelector.valid(match != null, GAME_TYPE_DARTZEE))
            {
                return
            }

            val selectedPlayers = playerSelector.getSelectedPlayers()

            val scrn = ScreenCache.getScreen(DartzeeRuleSetupScreen::class.java)
            scrn.setState(match, selectedPlayers)
            ScreenCache.switchScreen(scrn)
        }
        else
        {
            Debug.stackTrace("Unexpected screen state. GameType = ${gameTypeComboBox.getGameType()}")
        }
    }
}
