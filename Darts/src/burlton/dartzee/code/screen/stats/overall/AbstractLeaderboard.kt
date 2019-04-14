package burlton.dartzee.code.screen.stats.overall

import burlton.core.code.util.Debug
import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.TableUtil
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.sql.SQLException
import java.util.*
import javax.swing.JPanel

abstract class AbstractLeaderboard: JPanel(), ActionListener
{
    var builtTable = false

    abstract fun buildTable()
    abstract fun getTabName(): String

    override fun actionPerformed(e: ActionEvent?)
    {
        buildTable()
    }

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
                    val localId = rs.getLong(3)
                    val score = rs.getInt(4)

                    val playerFlag = PlayerEntity.getPlayerFlag(strategy == -1)

                    val row = arrayOf<Any>(playerFlag, playerName, localId, score)
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
}