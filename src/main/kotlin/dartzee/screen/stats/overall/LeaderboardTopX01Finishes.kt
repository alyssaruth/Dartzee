package dartzee.screen.stats.overall

import dartzee.bean.ScrollTableDartsGame
import dartzee.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import dartzee.utils.PreferenceUtil
import java.awt.BorderLayout
import javax.swing.JPanel

class LeaderboardTopX01Finishes : AbstractLeaderboard() {
    val tableTopFinishes = ScrollTableDartsGame()
    private val panelTopFinishesFilters = JPanel()

    init {
        layout = BorderLayout(0, 0)

        add(tableTopFinishes)
        tableTopFinishes.setRowHeight(23)
        add(panelTopFinishesFilters, BorderLayout.NORTH)
        panelTopFinishesFilters.add(panelPlayerFilters)
        tableTopFinishes.setRowName("finish", "finishes")
        panelPlayerFilters.addActionListener(this)
    }

    override fun getTabName() = "X01 Finishes"

    override fun buildTable() {
        val extraWhereSql = panelPlayerFilters.getWhereSql()

        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)

        val sb = StringBuilder()
        sb.append(" SELECT p.Strategy, p.Name, g.LocalId, xf.Finish")
        sb.append(" FROM X01Finish xf, Player p, Game g")
        sb.append(" WHERE xf.PlayerId = p.RowId")
        sb.append(" AND xf.GameId = g.RowId")
        if (!extraWhereSql.isEmpty()) {
            sb.append(" AND p.$extraWhereSql")
        }
        sb.append(" ORDER BY Finish DESC, xf.DtCreation ASC")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")

        val sql = sb.toString()
        buildStandardLeaderboard(tableTopFinishes, sql, "Finish")
    }
}
