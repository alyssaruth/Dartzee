package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import burlton.dartzee.code.utils.PreferenceUtil
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
        val zzParticipants = prepareParticipantTempTable()

        val sql = getTopX01FinishSql(zzParticipants)

        buildStandardLeaderboard(tableTopFinishes, sql, "Finish", true)

        DatabaseUtil.dropTable(zzParticipants)
    }

    private fun prepareParticipantTempTable(): String
    {
        val extraWhereSql = playerFilterPanelTopFinishes.whereSql

        val zzParticipants = DatabaseUtil.createTempTable("FinishedParticipants", "Strategy INT, PlayerName VARCHAR(25), LocalGameId INT, ParticipantId VARCHAR(36), RoundNumber INT")
        zzParticipants ?: return ""

        val sbPt = StringBuilder()
        sbPt.append("INSERT INTO $zzParticipants ")
        sbPt.append(" SELECT p.Strategy, p.Name, g.LocalId, pt.RowId, CEIL(CAST(FinalScore AS DECIMAL)/3)")
        sbPt.append(" FROM Player p, Participant pt, Game g")
        sbPt.append(" WHERE pt.GameId = g.RowId")
        sbPt.append(" AND g.GameType = $GAME_TYPE_X01")
        sbPt.append(" AND pt.FinalScore > -1")
        sbPt.append(" AND pt.PlayerId = p.RowId")
        if (!extraWhereSql.isEmpty())
        {
            sbPt.append(" AND p.$extraWhereSql")
        }

        DatabaseUtil.executeUpdate(sbPt.toString())
        DatabaseUtil.executeUpdate("CREATE INDEX zzParticipantIdRoundNumber ON zzFinishedParticipants(ParticipantId, RoundNumber)")

        return zzParticipants
    }

    /**
     * N.B. It is *wrong* to specify that drtLast.Ordinal = 3, as the finish might have been accomplished in two darts.
     *
     * This clause isn't actually needed, because we enforce that drtLast is a double and that it's score subtracted from the
     * starting score is 0, so it must be a finish dart (and therefore the last).
     */
    private fun getTopX01FinishSql(zzFinishedParticipants: String): String
    {
        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)

        val sb = StringBuilder()
        sb.append("SELECT zz.Strategy, zz.PlayerName, zz.LocalGameId, drtFirst.StartingScore")
        sb.append(" FROM Dart drtFirst, Round rnd, $zzFinishedParticipants zz")
        sb.append(" WHERE drtFirst.RoundId = rnd.RowId")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND rnd.ParticipantId = zz.ParticipantId")
        sb.append(" AND rnd.RoundNumber = zz.RoundNumber")
        sb.append(" ORDER BY drtFirst.StartingScore DESC")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")

        return sb.toString()
    }
}