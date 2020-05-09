package dartzee.screen.stats.player

import dartzee.`object`.Dart
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
import dartzee.utils.DatabaseUtil
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.sql.SQLException
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

class PlayerStatisticsScreen : EmbeddedScreen()
{
    private var hmLocalIdToWrapper = mapOf<Long, GameWrapper>()
    private var hmLocalIdToWrapperOther = mutableMapOf<Long, GameWrapper>()
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

        hmLocalIdToWrapper = retrieveGameData(player!!.rowId)
        hmLocalIdToWrapperOther.clear()

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

        val zzParticipants = buildParticipantTable(playerId)
        zzParticipants ?: return hm

        val sb = StringBuilder()
        sb.append(" SELECT zz.LocalId, zz.GameParams, zz.DtCreation, zz.DtFinish, zz.FinalScore, ")
        sb.append(" drt.RoundNumber,")
        sb.append(" drt.Ordinal, drt.Score, drt.Multiplier, drt.StartingScore, drt.SegmentType")
        sb.append(" FROM Dart drt, $zzParticipants zz")
        sb.append(" WHERE drt.ParticipantId = zz.ParticipantId")
        sb.append(" AND drt.PlayerId = zz.PlayerId")

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
            logger.logSqlException(sb.toString(), "", sqle)
        }
        finally
        {
            DatabaseUtil.dropTable(zzParticipants)
        }

        return hm
    }
    private fun buildParticipantTable(playerId: String): String?
    {
        val tmp = DatabaseUtil.createTempTable("ParticipantsForStats", "LocalId INT, GameParams VARCHAR(255), DtCreation TIMESTAMP, DtFinish TIMESTAMP, PlayerId VARCHAR(36), ParticipantId VARCHAR(36), FinalScore INT")
        tmp ?: return null

        val sb = StringBuilder()
        sb.append(" INSERT INTO $tmp")
        sb.append(" SELECT g.LocalId, g.GameParams, g.DtCreation, g.DtFinish, pt.PlayerId, pt.RowId AS ParticipantId, pt.FinalScore ")
        sb.append(" FROM Participant pt, Game g")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND pt.PlayerId = '$playerId'")
        sb.append(" AND g.GameType = '$gameType'")

        DatabaseUtil.executeUpdate("" + sb)

        DatabaseUtil.executeUpdate("CREATE INDEX ${tmp}_PlayerId_ParticipantId ON $tmp(PlayerId, ParticipantId)")
        return tmp
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
