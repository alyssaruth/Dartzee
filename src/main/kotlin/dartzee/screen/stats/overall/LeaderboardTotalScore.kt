package dartzee.screen.stats.overall

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.util.TableUtil
import dartzee.core.util.sortedBy
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.InjectedThings.partyMode
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.ResourceCache
import dartzee.utils.combinePlayerFlags
import dartzee.utils.doesHighestWin
import dartzee.utils.getFilterPanel
import dartzee.utils.getImageForTableRow
import dartzee.utils.getRawImageForTableRow
import dartzee.utils.splitAvatar
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionListener
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSeparator
import javax.swing.SwingConstants

class LeaderboardTotalScore(private val gameType: GameType, gameParams: String? = null) :
    AbstractLeaderboard(), ActionListener {
    private val panelGameParams = getFilterPanel(gameType)

    private val panelFilters = JPanel()
    private val table = ScrollTableDartsGame()
    private val panelBestOrWorst = RadioButtonPanel()
    private val rdbtnBest = JRadioButton("Best")
    private val rdbtnWorst = JRadioButton("Worst")
    private val panelTeamsOrIndividuals = RadioButtonPanel()
    private val rdbtnBoth = JRadioButton("All")
    private val rdbtnTeams = JRadioButton("Teams")
    private val rdbtnIndividuals = JRadioButton("Individuals")

    init {
        layout = BorderLayout(0, 0)
        table.setRowHeight(23)

        panelBestOrWorst.add(rdbtnBest)
        panelBestOrWorst.add(rdbtnWorst)
        panelTeamsOrIndividuals.add(rdbtnBoth)
        panelTeamsOrIndividuals.add(rdbtnTeams)
        panelTeamsOrIndividuals.add(rdbtnIndividuals)

        add(panelFilters, BorderLayout.NORTH)

        if (!partyMode) {
            panelFilters.add(panelGameParams)
            panelFilters.add(makeSeparator())
        }

        panelFilters.add(panelTeamsOrIndividuals)
        panelFilters.add(makeSeparator())

        if (!partyMode) {
            panelFilters.add(panelPlayerFilters)
            panelFilters.add(makeSeparator())
        }

        panelFilters.add(panelBestOrWorst)
        add(table, BorderLayout.CENTER)

        gameParams?.let(panelGameParams::setGameParams)

        panelGameParams.addActionListener(this)
        panelPlayerFilters.addActionListener(this)
        panelTeamsOrIndividuals.addActionListener(this)
        panelBestOrWorst.addActionListener(this)
    }

    private fun makeSeparator() =
        JSeparator(SwingConstants.VERTICAL).also { it.preferredSize = Dimension(2, 20) }

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

        if (partyMode) {
            table.setRowHeight(50)

            val font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 20f)
            table.setTableFont(font)
        }

        table.setColumnWidths("35;50")
        table.sortBy(0, false)
    }

    private fun retrieveDatabaseRowsForLeaderboard(): List<Array<Any>> {
        val individuals = if (rdbtnTeams.isSelected) emptyList() else retrieveSingleParticipants()
        val teams = if (rdbtnIndividuals.isSelected) emptyList() else retrieveTeams()

        val descending = doesHighestWin(gameType) != rdbtnWorst.isSelected
        val sorted =
            (individuals + teams)
                .sortedBy(descending) { it.score }
                .take(preferenceService.get(Preferences.leaderboardSize))
        return getRankedRowsForTable(sorted)
    }

    private fun retrieveSingleParticipants(): List<LeaderboardEntry> {
        val gameParams = panelGameParams.getGameParams()
        val playerWhereSql = panelPlayerFilters.getWhereSql()

        val sb = StringBuilder()
        sb.append("SELECT p.Strategy, p.Name, p.PlayerImageId, g.LocalId, pt.FinalScore")
        sb.append(" FROM Participant pt, Game g, Player p")
        sb.append(" WHERE pt.GameId = g.RowId")
        sb.append(" AND pt.PlayerId = p.RowId")
        sb.append(" AND g.GameType = '$gameType'")
        sb.append(" AND g.GameParams = '$gameParams'")
        sb.append(" AND pt.FinalScore > -1")

        if (playerWhereSql.isNotEmpty()) {
            sb.append(" AND p.$playerWhereSql")
        }

        appendOrderBy(sb, "pt")

        return mainDatabase.retrieveAsList(sb) { rs ->
            val strategy = rs.getString("Strategy")
            val playerName = rs.getString("Name")
            val localId = rs.getLong("LocalId")
            val score = rs.getInt("FinalScore")

            val flag =
                if (partyMode) getImageForTableRow(rs.getString("PlayerImageId"))
                else PlayerEntity.getPlayerFlag(strategy.isEmpty())

            LeaderboardEntry(score, listOf(flag, playerName, localId, score))
        }
    }

    private fun retrieveTeams(): List<LeaderboardEntry> {
        val gameParams = panelGameParams.getGameParams()
        val playerWhereSql = panelPlayerFilters.getWhereSql()

        val sb = StringBuilder()
        sb.append(
            "SELECT p1.Strategy, p2.Strategy AS Strategy2, p1.Name, p2.Name as Name2, " +
                "p1.PlayerImageId, p2.PlayerImageId as PlayerImageId2, g.LocalId, t.FinalScore"
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

        appendOrderBy(sb, "t")

        return mainDatabase.retrieveAsList(sb) { rs ->
            val strategy = rs.getString("Strategy")
            val strategy2 = rs.getString("Strategy2")
            val playerName1 = rs.getString("Name")
            val playerName2 = rs.getString("Name2")
            val localId = rs.getLong("LocalId")
            val score = rs.getInt("FinalScore")

            val icon =
                if (partyMode) {
                    val imageIdOne = rs.getString("PlayerImageId")
                    val imageIdTwo = rs.getString("PlayerImageId2")

                    val imgOne = getRawImageForTableRow(imageIdOne)
                    val imgTwo = getRawImageForTableRow(imageIdTwo)
                    splitAvatar(imgOne, imgTwo, null, true)
                } else {
                    val playerFlag = PlayerEntity.getPlayerFlag(strategy.isEmpty())
                    val playerFlag2 = PlayerEntity.getPlayerFlag(strategy2.isEmpty())
                    combinePlayerFlags(playerFlag, playerFlag2)
                }

            val playerName = "$playerName1 & $playerName2"

            LeaderboardEntry(score, listOf(icon, playerName, localId, score))
        }
    }

    private fun appendOrderBy(sb: StringBuilder, tableAlias: String) {
        val leaderboardSize = preferenceService.get(Preferences.leaderboardSize)
        val orderStr = if (doesHighestWin(gameType) == rdbtnWorst.isSelected) "ASC" else "DESC"
        sb.append(" ORDER BY $tableAlias.FinalScore $orderStr")
        sb.append(" FETCH FIRST $leaderboardSize ROWS ONLY")
    }
}
