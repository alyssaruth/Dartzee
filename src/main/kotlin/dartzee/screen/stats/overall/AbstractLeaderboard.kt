package dartzee.screen.stats.overall

import dartzee.bean.PlayerTypeFilterPanel
import dartzee.bean.ScrollTableDartsGame
import dartzee.core.util.TableUtil
import dartzee.db.PlayerEntity
import dartzee.utils.DatabaseUtil
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JPanel

abstract class AbstractLeaderboard: JPanel(), ActionListener
{
    val panelPlayerFilters = PlayerTypeFilterPanel()

    private var builtTable = false

    abstract fun buildTable()
    abstract fun getTabName(): String

    override fun actionPerformed(e: ActionEvent?) = buildTable()

    fun buildTableFirstTime()
    {
        if (!builtTable)
        {
            buildTable()
            builtTable = true
        }
    }

    /**
     * Build a standard leaderboard table, which contains the flag, name, Game ID and a custom 'score' column.
     */
    protected fun buildStandardLeaderboard(table: ScrollTableDartsGame, sql: String, scoreColumnName: String, desc: Boolean)
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Game")
        model.addColumn(scoreColumnName)

        val rows = retrieveDatabaseRowsForLeaderboard(sql)
        model.addRows(rows)

        table.model = model
        table.setColumnWidths("25")
        table.sortBy(3, desc)
    }

    private fun retrieveDatabaseRowsForLeaderboard(sqlStr: String): List<Array<Any>>
    {
        val rows = mutableListOf<Array<Any>>()

        DatabaseUtil.executeQuery(sqlStr).use { rs ->
            while (rs.next())
            {
                val strategyXml = rs.getString("StrategyXml")
                val playerName = rs.getString("Name")
                val localId = rs.getLong("LocalId")
                val score = rs.getInt("Finish")

                val playerFlag = PlayerEntity.getPlayerFlag(strategyXml.isEmpty())

                val row = arrayOf<Any>(playerFlag, playerName, localId, score)
                rows.add(row)
            }
        }

        return rows
    }
}