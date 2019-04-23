package burlton.dartzee.code.screen.stats.player

import burlton.core.code.obj.HandyArrayList
import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.PlayerSelectDialog
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.stats.GameWrapper
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.getAllChildComponentsForType
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.sql.SQLException
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

class PlayerStatisticsScreen : EmbeddedScreen()
{
    private var hmLocalIdToWrapper = mutableMapOf<Long, GameWrapper>()
    private var hmLocalIdToWrapperOther = mutableMapOf<Long, GameWrapper>()
    private var filteredGames = HandyArrayList<GameWrapper>()
    private var filteredGamesOther = HandyArrayList<GameWrapper>()

    private var gameType = -1
    private var player: PlayerEntity? = null

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

    init
    {
        add(filterPanels, BorderLayout.NORTH)
        add(tabbedPane, BorderLayout.CENTER)

        filterPanels.add(filterPanel)
        filterPanels.add(filterPanelOther)
        filterPanels.add(btnAdd)

        btnAdd.addActionListener(this)
    }

    override fun getScreenName() = "Statistics for $player"

    override fun initialise()
    {
        filterPanel.init(player!!, gameType, false)
        filterPanelOther.isVisible = false
        btnAdd.isVisible = true

        hmLocalIdToWrapper = retrieveGameData(player!!.rowId)
        hmLocalIdToWrapperOther.clear()

        resetTabs()
        buildTabs()
    }

    /**
     * Called when popping this up in a dialog after simulating games from the player amendment dialog (for AIs)
     */
    fun initFake(hmGameIdToWrapper: MutableMap<Long, GameWrapper>)
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

        if (gameType == GAME_TYPE_X01)
        {
            tabbedPane.addTab("Finish Breakdown", null, tabFinishing, null)
            tabbedPane.addTab("Checkout %", null, tabCheckoutPercent, null)
            tabbedPane.addTab("Top Finishes", null, tabTopFinishes, null)
            tabbedPane.addTab("Dart Average", null, tabThreeDartAverage, null)
            tabbedPane.addTab("Total Darts", null, tabTotalDarts, null)
            tabbedPane.addTab("Three Dart Scores", null, tabThreeDartScores, null)
        }
        else if (gameType == GAME_TYPE_GOLF)
        {
            tabbedPane.addTab("Hole Breakdown", null, tabHoleBreakdown, null)
            tabbedPane.addTab("Scorecards", null, tabBestRounds, null)
            tabbedPane.addTab("Optimal Scorecard", null, tabOptimalScorecard, null)
            tabbedPane.addTab("All Scores", null, tabAllScores, null)
        }
        else if (gameType == GAME_TYPE_ROUND_THE_CLOCK)
        {
            tabbedPane.addTab("Total Darts", null, tabTotalClockDarts, null)
        }
    }

    private fun addComparison()
    {
        val player = PlayerSelectDialog.selectPlayer() ?: return //Cancelled

        filterPanelOther.init(player, gameType, true)
        filterPanelOther.isVisible = true
        btnAdd.isVisible = false

        hmLocalIdToWrapperOther = retrieveGameData(player.rowId)
        buildTabs()
    }

    fun removeComparison()
    {
        filterPanelOther.isVisible = false
        btnAdd.isVisible = true
        hmLocalIdToWrapperOther = mutableMapOf()

        buildTabs()
    }

    private fun retrieveGameData(playerId: String): MutableMap<Long, GameWrapper>
    {
        val hm = mutableMapOf<Long, GameWrapper>()

        val sb = StringBuilder()
        sb.append(" SELECT g.LocalId, g.GameParams, g.DtCreation, g.DtFinish,")
        sb.append(" gp.FinalScore, ")
        sb.append(" rnd.RoundNumber,")
        sb.append(" drt.Ordinal, drt.Score, drt.Multiplier, drt.StartingScore, drt.SegmentType")
        sb.append(" FROM Dart drt, Round rnd, Participant gp, Game g")
        sb.append(" WHERE drt.RoundId = rnd.RowId")
        sb.append(" AND rnd.ParticipantId = gp.RowId")
        sb.append(" AND gp.GameId = g.RowId")
        sb.append(" AND gp.PlayerId = '$playerId'")
        sb.append(" AND g.GameType = $gameType")

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val gameId = rs.getLong("LocalId")
                    val gameParams = rs.getString("GameParams")
                    val dtStart = rs.getTimestamp("DtCreation")
                    val dtFinish = rs.getTimestamp("DtFinish")
                    val numberOfDarts = rs.getInt("FinalScore")
                    val roundNumber = rs.getInt("RoundNumber")
                    val ordinal = rs.getInt("Ordinal")
                    val score = rs.getInt("Score")
                    val multiplier = rs.getInt("Multiplier")
                    val startingScore = rs.getInt("StartingScore")
                    val segmentType = rs.getInt("SegmentType")

                    val wrapper = hm[gameId] ?: GameWrapper(gameId, gameParams, dtStart, dtFinish, numberOfDarts)
                    hm[gameId] = wrapper

                    val dart = Dart(score, multiplier)
                    dart.ordinal = ordinal
                    dart.startingScore = startingScore
                    dart.segmentType = segmentType
                    wrapper.addDart(roundNumber, dart)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
        }

        return hm
    }

    fun buildTabs()
    {
        filteredGames = populateFilteredGames(hmLocalIdToWrapper, filterPanel)
        filteredGamesOther = populateFilteredGames(hmLocalIdToWrapperOther, filterPanelOther)

        //Update the tabs
        val tabs = getAllChildComponentsForType(this, AbstractStatisticsTab::class.java)
        for (tab in tabs)
        {
            tab.setFilteredGames(filteredGames, filteredGamesOther)
            tab.populateStats()
        }
    }

    private fun populateFilteredGames(hmGameIdToWrapper: MutableMap<Long, GameWrapper>,
                                      filterPanel: PlayerStatisticsFilterPanel): HandyArrayList<GameWrapper>
    {
        val allGames = HandyArrayList(hmGameIdToWrapper.values)
        if (!filterPanel.isVisible)
        {
            return allGames
        }

        val filteredGames = allGames.filter { g -> filterPanel.includeGame(g) }
        filterPanel.update(filteredGames)
        return HandyArrayList(filteredGames)
    }

    fun setVariables(gameType: Int, player: PlayerEntity)
    {
        this.gameType = gameType
        this.player = player
    }

    override fun getBackTarget(): EmbeddedScreen
    {
        return ScreenCache.getPlayerManagementScreen()
    }


    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAdd -> addComparison()
            else -> super.actionPerformed(arg0)
        }
    }
}
