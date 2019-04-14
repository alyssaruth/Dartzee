package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.desktopcore.code.util.getEndOfTimeSqlString
import java.awt.BorderLayout
import javax.swing.JPanel

const val TOTAL_ROUND_SCORE_SQL_STR = "(drtFirst.StartingScore - drtLast.StartingScore) + (drtLast.score * drtLast.multiplier)"

class LeaderboardTopX01Finishes: AbstractLeaderboard()
{
    private val tableTopFinishes = ScrollTableDartsGame()
    private val playerFilterPanelTopFinishes = PlayerTypeFilterPanel()
    private val panelTopFinishesFilters = JPanel()

    init
    {
        layout = BorderLayout(0, 0)

        add(tableTopFinishes)
        tableTopFinishes.setRowHeight(23)
        add(panelTopFinishesFilters, BorderLayout.NORTH)
        panelTopFinishesFilters.add(playerFilterPanelTopFinishes)
        tableTopFinishes.setRowName("finish", "finishes")
        playerFilterPanelTopFinishes.addActionListener(this)
    }

    override fun getTabName() = "X01 Finishes"

    override fun buildTable()
    {
        val sql = getTopX01FinishSql()

        buildStandardLeaderboard(tableTopFinishes, sql, "Finish", true)
    }

    /**
     * N.B. It is *wrong* to specify that drtLast.Ordinal = 3, as the finish might have been accomplished in two darts.
     *
     * This clause isn't actually needed, because we enforce that drtLast is a double and that it's score subtracted from the
     * starting score is 0, so it must be a finish dart (and therefore the last).
     */
    fun getTopX01FinishSql(): String
    {
        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)
        val extraWhereSql = playerFilterPanelTopFinishes.whereSql

        val sb = StringBuilder()
        sb.append("SELECT p.Strategy, p.Name, g.LocalId, $TOTAL_ROUND_SCORE_SQL_STR")
        sb.append(" FROM Dart drtFirst, Dart drtLast, Round rnd, Participant pt, Player p, Game g")
        sb.append(" WHERE drtFirst.RoundId = rnd.RowId")
        sb.append(" AND drtLast.RoundId = rnd.RowId")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.PlayerId = p.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_X01")
        sb.append(" AND pt.DtFinished < ${getEndOfTimeSqlString()}")
        sb.append(" AND drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0")
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