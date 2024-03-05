package dartzee.screen

import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelX01
import dartzee.bean.GameSetupPlayerSelector
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.util.StringUtil
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartsMatchEntity
import dartzee.db.DartsMatchEntity.Companion.constructPointsJson
import dartzee.db.DartzeeRuleEntity
import dartzee.db.MAX_PLAYERS
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.screen.dartzee.DartzeeRuleSetupScreen
import dartzee.utils.InjectedThings.gameLauncher
import dartzee.utils.getFilterPanel
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.border.TitledBorder
import net.miginfocom.swing.MigLayout

class GameSetupScreen : EmbeddedScreen() {
    private val panelSetup = JPanel()
    private val panelGameType = JPanel()
    private val panelPlayers = JPanel()
    private val launchPanel = JPanel()
    private val btnLaunch = JButton("Launch Game")
    val playerSelector = GameSetupPlayerSelector()
    val gameTypeComboBox = ComboBoxGameType()
    private val panelGameTypeCb = JPanel()
    var gameParamFilterPanel: GameParamFilterPanel = GameParamFilterPanelX01()

    val matchConfigPanel = RadioButtonPanel()
    val rdbtnSingleGame = JRadioButton("Single Game")
    val rdbtnFirstTo = JRadioButton("First to")
    val rdbtnPoints = JRadioButton("Points-based")
    val spinnerWins = JSpinner()
    val spinnerGames = JSpinner()
    val lblWins = JLabel("  wins")
    val lblGames = JLabel("  games  ")
    val panelPointBreakdown = JPanel()

    val spinners =
        List(MAX_PLAYERS) { ix ->
            JSpinner().also { it.model = SpinnerNumberModel(maxOf(0, 4 - ix), 0, 20, 1) }
        }
    private val labels = List(MAX_PLAYERS) { ix -> JLabel(StringUtil.convertOrdinalToText(ix + 1)) }

    init {
        panelSetup.layout = BorderLayout(0, 0)
        panelGameType.border =
            TitledBorder(null, "Game Type", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelSetup.add(panelGameType, BorderLayout.NORTH)
        panelGameType.layout = GridLayout(0, 1, 0, 0)
        add(panelSetup, BorderLayout.NORTH)
        panelGameType.add(panelGameTypeCb)
        panelGameTypeCb.add(gameTypeComboBox)
        panelGameType.add(gameParamFilterPanel)
        panelPlayers.border =
            TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        add(panelPlayers, BorderLayout.CENTER)
        panelPlayers.layout = BorderLayout(0, 0)
        panelPlayers.add(launchPanel, BorderLayout.SOUTH)
        launchPanel.add(btnLaunch)
        panelPlayers.add(playerSelector, BorderLayout.CENTER)
        panelSetup.add(matchConfigPanel, BorderLayout.CENTER)
        matchConfigPanel.border =
            TitledBorder(null, "Match Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null)
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
        labels.forEachIndexed { ix, lbl ->
            panelPointBreakdown.add(lbl, "flowy,cell $ix 0,alignx center")
        }
        spinners.forEachIndexed { ix, spinner ->
            panelPointBreakdown.add(spinner, "cell $ix 0,alignx center")
        }
        btnLaunch.name = "LaunchButton"

        matchConfigPanel.addActionListener(this)
        gameTypeComboBox.addActionListener(this)
        btnLaunch.addActionListener(this)
    }

    override fun initialise() {
        playerSelector.init()

        refreshGameParamFilterPanel()

        toggleComponents()
    }

    override fun actionPerformed(arg0: ActionEvent) {
        if (arg0.source === btnLaunch) {
            launchGame()
        } else if (arg0.source === gameTypeComboBox) {
            refreshGameParamFilterPanel()
        } else if (arg0.source != btnNext && arg0.source != btnBack) {
            toggleComponents()
        } else {
            super.actionPerformed(arg0)
        }
    }

    private fun refreshGameParamFilterPanel() {
        // Remove what's already there
        gameParamFilterPanel.removeActionListener(this)
        panelGameType.remove(gameParamFilterPanel)

        gameParamFilterPanel = getFilterPanel(gameTypeComboBox.getGameType())
        panelGameType.add(gameParamFilterPanel)
        gameParamFilterPanel.addActionListener(this)

        panelGameType.revalidate()

        toggleComponents()
    }

    private fun toggleComponents() {
        if (rdbtnSingleGame.isSelected) {
            btnLaunch.text = "Launch Game"
        } else {
            btnLaunch.text = "Launch Match"
        }

        spinnerWins.isVisible = rdbtnFirstTo.isSelected
        lblWins.isVisible = rdbtnFirstTo.isSelected

        spinnerGames.isVisible = rdbtnPoints.isSelected
        lblGames.isVisible = rdbtnPoints.isSelected

        if (rdbtnPoints.isSelected) {
            matchConfigPanel.add(panelPointBreakdown, "cell 0 3,span")
        } else {
            matchConfigPanel.remove(panelPointBreakdown)
        }

        val customDartzee =
            gameTypeComboBox.getGameType() == GameType.DARTZEE &&
                gameParamFilterPanel.getGameParams() == ""
        btnLaunch.isVisible = !customDartzee
        toggleNextVisibility(customDartzee)

        invalidate()
        revalidate()
        validate()
    }

    private fun getGameParams() = gameParamFilterPanel.getGameParams()

    private fun launchGame() {
        val selectedPlayers = playerSelector.getSelectedPlayers()
        val match = factoryMatch()
        if (!playerSelector.valid(match != null)) {
            return
        }

        val rules = retrieveDartzeeRules()
        val launchParams =
            GameLaunchParams(
                selectedPlayers,
                gameTypeComboBox.getGameType(),
                getGameParams(),
                playerSelector.pairMode(),
                rules
            )

        if (match == null) {
            gameLauncher.launchNewGame(launchParams)
        } else {
            gameLauncher.launchNewMatch(match, launchParams)
        }
    }

    private fun retrieveDartzeeRules(): List<DartzeeRuleDto>? {
        if (gameTypeComboBox.getGameType() != GameType.DARTZEE) {
            return null
        }

        val dartzeeTemplate =
            (gameParamFilterPanel as GameParamFilterPanelDartzee).getSelectedTemplate()!!
        val rules = DartzeeRuleEntity().retrieveForTemplate(dartzeeTemplate.rowId)
        return rules.map { it.toDto() }
    }

    private fun factoryMatch(): DartsMatchEntity? {
        return when {
            rdbtnFirstTo.isSelected -> DartsMatchEntity.factoryFirstTo(spinnerWins.value as Int)
            rdbtnPoints.isSelected ->
                DartsMatchEntity.factoryPoints(spinnerGames.value as Int, getPointsJson())
            else -> null
        }
    }

    private fun getPointsJson(): String {
        return constructPointsJson(
            spinners[0].value as Int,
            spinners[1].value as Int,
            spinners[2].value as Int,
            spinners[3].value as Int,
            spinners[4].value as Int,
            spinners[5].value as Int
        )
    }

    override fun getScreenName() = "Game Setup"

    override fun nextPressed() {
        val selectedPlayers = playerSelector.getSelectedPlayers()
        val match = factoryMatch()
        if (!playerSelector.valid(match != null)) {
            return
        }

        val scrn = DartzeeRuleSetupScreen(match, selectedPlayers, playerSelector.pairMode())
        ScreenCache.switch(scrn)
    }
}
