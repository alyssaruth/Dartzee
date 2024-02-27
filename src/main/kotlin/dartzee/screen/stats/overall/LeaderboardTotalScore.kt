package dartzee.screen.stats.overall

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.util.TableUtil
import dartzee.core.util.sortedBy
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import dartzee.utils.PreferenceUtil
import dartzee.utils.combinePlayerFlags
import dartzee.utils.doesHighestWin
import dartzee.utils.getFilterPanel
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.JPanel
import javax.swing.JRadioButton

class LeaderboardTotalScore(private val gameType: GameType) :
    AbstractLeaderboard(), ActionListener {
    private val panelGameParams = getFilterPanel(gameType)

    private val panelFilters = JPanel()
    private val table = ScrollTableDartsGame()
    private val panelBestOrWorst = RadioButtonPanel()
    private val rdbtnBest = JRadioButton("Best")
    private val rdbtnWorst = JRadioButton("Worst")

    init {
        layout = BorderLayout(0, 0)

        panelGameParams.addActionListener(this)
        panelPlayerFilters.addActionListener(this)
        table.setRowHeight(23)
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
        add(table, BorderLayout.CENTER)

        panelBestOrWorst.addActionListener(this)
    }

    override fun getTabName() = gameType.getDescription()

    override fun buildTable() {
        val model = TableUtil.DefaultModel()
        model.addColumn("#")
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Game")
        model.addColumn("Score")

        val rows = retrieveDatabaseRowsForLeaderboard()
        model.addRows(rows)

        table.model = model
        table.setColumnWidths("35;50")
        table.sortBy(0, false)
    }

    private fun retrieveDatabaseRowsForLeaderboard(): List<Array<Any>> {
        val individualRows = retrieveEntries(singleParticipantSql())
        val teamRows = retrieveEntries(teamSql())

        val descending = doesHighestWin(gameType) != rdbtnWorst.isSelected
        val sorted =
            (individualRows + teamRows)
                .sortedBy(descending) { it.score }
                .take(PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE))
        return getRankedRowsForTable(sorted)
    }

    private fun singleParticipantSql(): String {
        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)
        val gameParams = panelGameParams.getGameParams()
        val playerWhereSql = panelPlayerFilters.getWhereSql()

        val sb = StringBuilder()
        sb.append(
            "SELECT p.Strategy AS Strategy1, 'NULL' AS Strategy2, p.Name AS Name1, 'NULL' as Name2, g.LocalId, pt.FinalScore"
        )
        sb.append(" FROM Participant pt, Game g, Player p")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND pt.PlayerId = p.RowId")
        sb.append(" AND g.GameType = '$gameType'")
        sb.append(" AND g.GameParams = '$gameParams'")
        sb.append(" AND pt.FinalScore > -1")

        if (playerWhereSql.isNotEmpty()) {
            sb.append(" AND p.$playerWhereSql")
        }

        val orderStr = if (doesHighestWin(gameType) == rdbtnWorst.isSelected) "ASC" else "DESC"
        sb.append(" ORDER BY pt.FinalScore $orderStr")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")
        return sb.toString()
    }

    private fun teamSql(): String {
        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)
        val gameParams = panelGameParams.getGameParams()
        val playerWhereSql = panelPlayerFilters.getWhereSql()

        val sb = StringBuilder()
        sb.append(
            "SELECT p1.Strategy AS Strategy1, p2.Strategy AS Strategy2, p1.Name AS Name1, p2.Name as Name2, g.LocalId, t.FinalScore"
        )
        sb.append(" FROM Team t, Participant pt1, Participant pt2, Game g, Player p1, Player p2")
        sb.append(" WHERE t.GameId = g.RowId")
        sb.append(" AND g.GameType = '$gameType'")
        sb.append(" AND g.GameParams = '$gameParams'")
        sb.append(" AND pt1.TeamId = t.RowId")
        sb.append(" AND pt1.Ordinal = 0")
        sb.append(" AND pt2.TeamId = t.RowId")
        sb.append(" AND pt2.Ordinal = 1")
        sb.append(" AND pt1.PlayerId = p1.RowId")
        sb.append(" AND pt2.PlayerId = p2.RowId")
        sb.append(" AND t.FinalScore > -1")

        if (playerWhereSql.isNotEmpty()) {
            sb.append(" AND p1.$playerWhereSql")
            sb.append(" AND p2.$playerWhereSql")
        }

        val orderStr = if (doesHighestWin(gameType) == rdbtnWorst.isSelected) "ASC" else "DESC"
        sb.append(" ORDER BY t.FinalScore $orderStr")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")
        return sb.toString()
    }

    private fun retrieveEntries(sql: String): List<LeaderboardEntry> {
        val rows = mutableListOf<LeaderboardEntry>()

        mainDatabase.executeQuery(sql).use { rs ->
            while (rs.next()) {
                val strategy = rs.getString("Strategy1")
                val strategy2 = rs.getString("Strategy2")
                val playerName1 = rs.getString("Name1")
                val playerName2 = rs.getString("Name2")
                val localId = rs.getLong("LocalId")
                val score = rs.getInt("FinalScore")

                val playerFlag = PlayerEntity.getPlayerFlag(strategy.isEmpty())
                val playerFlag2 =
                    if (strategy2 != "NULL") PlayerEntity.getPlayerFlag(strategy2.isEmpty())
                    else null

                val combinedFlag = playerFlag2?.let { combinePlayerFlags(playerFlag, playerFlag2) }
                val flag = combinedFlag ?: playerFlag

                val playerName =
                    if (playerName2 != "NULL") "$playerName1 & $playerName2" else playerName1

                val entry = LeaderboardEntry(score, listOf(flag, playerName, localId, score))
                rows.add(entry)
            }
        }

        return rows.toList()
    }
}
