package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.DartsRegistry
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.desktopcore.code.bean.RadioButtonPanel
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JPanel
import javax.swing.JRadioButton

class OverallStatsTabTotalScore(private val gameType: Int) : JPanel(), ActionListener
{
    private val panelGameParams = GameEntity.getFilterPanel(gameType)

    private val panelFilters = JPanel()
    private val scrollPane = ScrollTableDartsGame()
    private val panelPlayerFilters = PlayerTypeFilterPanel()
    private val panelBestOrWorst = RadioButtonPanel()
    private val rdbtnBest = JRadioButton("Best")
    private val rdbtnWorst = JRadioButton("Worst")

    init
    {
        layout = BorderLayout(0, 0)

        panelGameParams.addActionListener(this)
        panelPlayerFilters.addActionListener(this)
        scrollPane.setRowHeight(23)
        add(panelFilters, BorderLayout.NORTH)
        panelFilters.add(panelGameParams)
        val horizontalStrut = Box.createHorizontalStrut(20)
        panelFilters.add(horizontalStrut)
        panelFilters.add(panelPlayerFilters)
        val horizontalStrut2 = Box.createHorizontalStrut(20)
        panelFilters.add(horizontalStrut2)
        panelFilters.add(panelBestOrWorst)
        panelBestOrWorst.add(rdbtnBest)
        panelBestOrWorst.add(rdbtnWorst)
        add(scrollPane, BorderLayout.CENTER)

        panelBestOrWorst.addActionListener(this)
    }

    fun buildTable()
    {
        val sql = getTotalScoreSql()
        OverallStatsScreen.Companion.buildStandardLeaderboard(scrollPane, sql, "Score", rdbtnWorst.isSelected)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        buildTable()
    }

    fun getTotalScoreSql() : String
    {
        val leaderboardSize = PreferenceUtil.getIntValue(DartsRegistry.PREFERENCES_INT_LEADERBOARD_SIZE)
        val gameParams = panelGameParams.gameParams
        val playerWhereSql = panelPlayerFilters.whereSql

        val sb = StringBuilder()
        sb.append("SELECT p.Strategy, p.Name, pt.GameId, pt.FinalScore")
        sb.append(" FROM Participant pt, Game g, Player p")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND pt.PlayerId = p.RowId")
        sb.append(" AND g.GameType = $gameType")
        sb.append(" AND g.GameParams = '$gameParams'")
        sb.append(" AND pt.FinalScore > -1")

        if (!playerWhereSql.isEmpty())
        {
            sb.append(" AND p.$playerWhereSql")
        }

        val orderStr = if (rdbtnBest.isSelected) "ASC" else "DESC"
        sb.append(" ORDER BY pt.FinalScore $orderStr")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")

        return sb.toString()
    }
}
