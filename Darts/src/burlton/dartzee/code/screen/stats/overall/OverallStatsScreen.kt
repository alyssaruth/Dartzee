package burlton.dartzee.code.screen.stats.overall

import burlton.core.code.util.Debug
import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.db.getAllGameTypes
import burlton.dartzee.code.db.getTypeDesc
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.utils.DartsRegistry
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.TableUtil
import burlton.desktopcore.code.util.getEndOfTimeSqlString
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.sql.SQLException
import java.util.*
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

const val TOTAL_ROUND_SCORE_SQL_STR = "(drtFirst.StartingScore - drtLast.StartingScore) + (drtLast.score * drtLast.multiplier)"

/**
 * Build a standard leaderboard table, which contains the flag, name, Game ID and a custom 'score' column.
 */
fun buildStandardLeaderboard(table: ScrollTableDartsGame, sql: String, scoreColumnName: String, desc: Boolean)
{
    val model = TableUtil.DefaultModel()
    model.addColumn("")
    model.addColumn("Player")
    model.addColumn("Game")
    model.addColumn(scoreColumnName)

    val rows = retrieveDatabaseRowsForLeaderboard(sql)
    for (row in rows)
    {
        model.addRow(row)
    }

    table.model = model
    table.setColumnWidths("25")
    table.sortBy(3, desc)
}

private fun retrieveDatabaseRowsForLeaderboard(sqlStr: String): ArrayList<Array<Any>>
{
    val rows = ArrayList<Array<Any>>()

    try
    {
        DatabaseUtil.executeQuery(sqlStr).use { rs ->
            while (rs.next())
            {
                val strategy = rs.getInt(1)
                val playerName = rs.getString(2)
                val gameId = rs.getLong(3)
                val score = rs.getInt(4)

                val playerFlag = PlayerEntity.getPlayerFlag(strategy == -1)

                val row = arrayOf<Any>(playerFlag, playerName, gameId, score)
                rows.add(row)
            }
        }
    }
    catch (sqle: SQLException)
    {
        Debug.logSqlException(sqlStr, sqle)
        DialogUtil.showError("Failed to build finishes leaderboard.")
    }

    return rows
}

class OverallStatsScreen : EmbeddedScreen()
{
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val panelX01Finishes = JPanel()
    private val tableTopFinishes = ScrollTableDartsGame()
    private val playerFilterPanelTopFinishes = PlayerTypeFilterPanel()
    private val panelTopFinishesFilters = JPanel()
    private val tabAchievements = OverallStatsTabAchievements()

    init
    {
        add(tabbedPane, BorderLayout.CENTER)

        addTotalScoreTabs()

        tabbedPane.addTab("X01 Finishes", null, panelX01Finishes, null)
        tabbedPane.addTab("Achievements", null, tabAchievements, null)
        panelX01Finishes.layout = BorderLayout(0, 0)

        panelX01Finishes.add(tableTopFinishes)
        tableTopFinishes.setRowHeight(23)
        panelX01Finishes.add(panelTopFinishesFilters, BorderLayout.NORTH)
        panelTopFinishesFilters.add(playerFilterPanelTopFinishes)
        playerFilterPanelTopFinishes.addActionListener(this)
    }

    /**
     * The total score tabs are added dynamically, so that adding a new game type will automatically update the leaderboard
     */
    private fun addTotalScoreTabs()
    {
        val gameTypes = getAllGameTypes()
        for (gameType in gameTypes)
        {
            val tabTitle = getTypeDesc(gameType)
            tabbedPane.addTab(tabTitle, null, OverallStatsTabTotalScore(gameType), null)
        }
    }

    override fun getScreenName(): String
    {
        return "Game Statistics"
    }

    override fun initialise()
    {
        val tabCount = tabbedPane.tabCount
        for (i in 0 until tabCount)
        {
            val tab = tabbedPane.getComponentAt(i)
            if (tab is OverallStatsTabTotalScore)
            {
                tab.buildTable()
            }
        }

        buildTopFinishesTable()
        tabAchievements.buildTable()
    }


    private fun buildTopFinishesTable()
    {
        val sql = getTopX01FinishSql()

        buildStandardLeaderboard(tableTopFinishes, sql, "Finish", true)
        tableTopFinishes.setRowName("finish", "finishes")
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (playerFilterPanelTopFinishes.isEventSource(arg0))
        {
            buildTopFinishesTable()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    /**
     * N.B. It is *wrong* to specify that drtLast.Ordinal = 3, as the finish might have been accomplished in two darts.
     *
     * This clause isn't actually needed, because we enforce that drtLast is a double and that it's score subtracted from the
     * starting score is 0, so it must be a finish dart (and therefore the last).
     */
    fun getTopX01FinishSql(): String
    {
        val leaderboardSize = PreferenceUtil.getIntValue(DartsRegistry.PREFERENCES_INT_LEADERBOARD_SIZE)
        val extraWhereSql = playerFilterPanelTopFinishes.whereSql


        val sb = StringBuilder()
        sb.append("SELECT p.Strategy, p.Name, pt.GameId, $TOTAL_ROUND_SCORE_SQL_STR")
        sb.append(" FROM Dart drtFirst, Dart drtLast, Round rnd, Participant pt, Player p")
        sb.append(" WHERE drtFirst.RoundId = rnd.RowId")
        sb.append(" AND drtLast.RoundId = rnd.RowId")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.PlayerId = p.RowId")
        sb.append(" AND pt.DtFinished < ${getEndOfTimeSqlString()}")
        sb.append("	AND drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0")
        sb.append(" AND drtLast.Multiplier = 2")

        if (!extraWhereSql.isEmpty())
        {
            sb.append(" AND p.$extraWhereSql")
        }

        sb.append(" ORDER BY $TOTAL_ROUND_SCORE_SQL_STR DESC")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")

        return sb.toString()
    }
}
