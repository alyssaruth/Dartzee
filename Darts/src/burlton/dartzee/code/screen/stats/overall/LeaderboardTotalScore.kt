package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.desktopcore.code.bean.RadioButtonPanel
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JPanel
import javax.swing.JRadioButton

class LeaderboardTotalScore(private val gameType: Int) : AbstractLeaderboard(), ActionListener
{
    private val panelGameParams = GameEntity.getFilterPanel(gameType)

    private val panelFilters = JPanel()
    private val scrollPane = ScrollTableDartsGame()
    private val panelBestOrWorst = RadioButtonPanel()
    private val rdbtnBest = JRadioButton("Best")
    private val rdbtnWorst = JRadioButton("Worst")

    init
    {
        layout = BorderLayout(0, 0)

        panelGameParams?.addActionListener(this)
        panelPlayerFilters.addActionListener(this)
        scrollPane.setRowHeight(23)
        add(panelFilters, BorderLayout.NORTH)

        if (panelGameParams != null)
        {
            panelFilters.add(panelGameParams)
        }

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

    override fun getTabName() = GameEntity.getTypeDesc(gameType)

    override fun buildTable()
    {
        val sql = getTotalScoreSql()
        buildStandardLeaderboard(scrollPane, sql, "Score", rdbtnWorst.isSelected)
    }

    private fun getTotalScoreSql() : String
    {
        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)
        val gameParams = panelGameParams?.getGameParams() ?: ""
        val playerWhereSql = panelPlayerFilters.getWhereSql()

        val sb = StringBuilder()
        sb.append("SELECT p.Strategy, p.Name, g.LocalId, pt.FinalScore")
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
