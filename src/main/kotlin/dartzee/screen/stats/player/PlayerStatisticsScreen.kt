package dartzee.screen.stats.player

import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.EmbeddedScreen
import dartzee.screen.PlayerSelectDialog
import dartzee.screen.ScreenCache
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.stats.player.golf.StatisticsTabGolfHoleBreakdown
import dartzee.screen.stats.player.golf.StatisticsTabGolfOptimalScorecard
import dartzee.screen.stats.player.golf.StatisticsTabGolfScorecards
import dartzee.screen.stats.player.rtc.StatisticsTabRoundTheClockHitRate
import dartzee.screen.stats.player.x01.*
import dartzee.stats.GameWrapper
import dartzee.stats.retrieveGameData
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

class PlayerStatisticsScreen : EmbeddedScreen()
{
    private var hmLocalIdToWrapper = mapOf<Long, GameWrapper>()
    private var hmLocalIdToWrapperOther = mapOf<Long, GameWrapper>()
    private var filteredGames = listOf<GameWrapper>()
    private var filteredGamesOther = listOf<GameWrapper>()

    var gameType: GameType = GameType.X01
    var player: PlayerEntity? = null

    //Components
    private val filterPanels = JPanel()
    private val filterPanel = PlayerStatisticsFilterPanel()
    private val filterPanelOther = PlayerStatisticsFilterPanel()
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val btnAdd = JButton("Add Comparison")

    //X01 tabs
    private val tabFinishing = StatisticsTabFinishBreakdown()
    private val tabCheckoutPercent = StatisticsTabX01CheckoutPercent()
    private val tabTopFinishes = StatisticsTabX01TopFinishes()
    private val tabThreeDartAverage = StatisticsTabX01ThreeDartAverage()
    private val tabTotalDarts = StatisticsTabTotalScore("Total Darts", 200)
    private val tabThreeDartScores = StatisticsTabX01ThreeDartScores()

    //Golf tabs
    private val tabHoleBreakdown = StatisticsTabGolfHoleBreakdown()
    private val tabAllScores = StatisticsTabTotalScore("Total Shots", 90)
    private val tabBestRounds = StatisticsTabGolfScorecards()
    private val tabOptimalScorecard = StatisticsTabGolfOptimalScorecard()

    //Round the Clock tabs
    private val tabTotalClockDarts = StatisticsTabTotalScore("Total Darts", 500)
    private val tabTargetBreakdown = StatisticsTabRoundTheClockHitRate()

    init
    {
        add(filterPanels, BorderLayout.NORTH)
        add(tabbedPane, BorderLayout.CENTER)

        filterPanels.add(filterPanel)
        filterPanels.add(filterPanelOther)
        filterPanels.add(btnAdd)

        btnAdd.addActionListener(this)
    }

    override fun getScreenName() = "${gameType.getDescription()} Statistics for $player"
    override fun getDesiredSize() = Dimension(1240, 700)

    override fun initialise()
    {
        filterPanel.init(player!!, gameType, false)
        filterPanelOther.isVisible = false
        btnAdd.isVisible = true

        hmLocalIdToWrapper = retrieveGameData(player!!.rowId, gameType)
        hmLocalIdToWrapperOther = mapOf()

        resetTabs()
        buildTabs()
    }

    /**
     * Called when popping this up in a dialog after simulating games from the player amendment dialog (for AIs)
     */
    fun initFake(hmGameIdToWrapper: Map<Long, GameWrapper>)
    {
        filterPanel.init(player!!, gameType, false)
        filterPanelOther.isVisible = false
        btnAdd.isVisible = false
        hideBackButton()

        this.hmLocalIdToWrapper = hmGameIdToWrapper

        resetTabs()
        buildTabs()
    }

    private fun resetTabs()
    {
        tabbedPane.removeAll()

        if (gameType == GameType.X01)
        {
            tabbedPane.addTab("Finish Breakdown", null, tabFinishing, null)
            tabbedPane.addTab("Checkout %", null, tabCheckoutPercent, null)
            tabbedPane.addTab("Top Finishes", null, tabTopFinishes, null)
            tabbedPane.addTab("Dart Average", null, tabThreeDartAverage, null)
            tabbedPane.addTab("Total Darts", null, tabTotalDarts, null)
            tabbedPane.addTab("Three Dart Scores", null, tabThreeDartScores, null)
        }
        else if (gameType == GameType.GOLF)
        {
            tabbedPane.addTab("Hole Breakdown", null, tabHoleBreakdown, null)
            tabbedPane.addTab("Scorecards", null, tabBestRounds, null)
            tabbedPane.addTab("Optimal Scorecard", null, tabOptimalScorecard, null)
            tabbedPane.addTab("All Scores", null, tabAllScores, null)
        }
        else if (gameType == GameType.ROUND_THE_CLOCK)
        {
            tabbedPane.addTab("Total Darts", null, tabTotalClockDarts, null)
            tabbedPane.addTab("Target Breakdown", null, tabTargetBreakdown, null)
        }
    }

    private fun addComparison()
    {
        val player = PlayerSelectDialog.selectPlayer() ?: return //Cancelled

        filterPanelOther.init(player, gameType, true)
        filterPanelOther.isVisible = true
        btnAdd.isVisible = false

        hmLocalIdToWrapperOther = retrieveGameData(player.rowId, gameType)
        buildTabs()
    }

    fun removeComparison()
    {
        filterPanelOther.isVisible = false
        btnAdd.isVisible = true
        hmLocalIdToWrapperOther = mapOf()

        buildTabs()
    }

    fun buildTabs()
    {
        filteredGames = populateFilteredGames(hmLocalIdToWrapper, filterPanel)
        filteredGamesOther = populateFilteredGames(hmLocalIdToWrapperOther, filterPanelOther)

        //Update the tabs
        val tabs = getAllChildComponentsForType<AbstractStatisticsTab>()
        for (tab in tabs)
        {
            tab.setFilteredGames(filteredGames, filteredGamesOther)
            tab.populateStats()
        }
    }

    private fun populateFilteredGames(hmGameIdToWrapper: Map<Long, GameWrapper>,
                                      filterPanel: PlayerStatisticsFilterPanel): List<GameWrapper>
    {
        val allGames = hmGameIdToWrapper.values
        if (!filterPanel.isVisible)
        {
            return allGames.toList()
        }

        val filteredGames = allGames.filter { g -> filterPanel.includeGame(g) }
        filterPanel.update(filteredGames)
        return filteredGames
    }

    fun setVariables(gameType: GameType, player: PlayerEntity)
    {
        this.gameType = gameType
        this.player = player
    }

    override fun getBackTarget() = ScreenCache.get<PlayerManagementScreen>()

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAdd -> addComparison()
            else -> super.actionPerformed(arg0)
        }
    }
}
