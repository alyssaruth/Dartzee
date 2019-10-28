package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.UtilitiesScreen
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import javax.swing.JPanel

class DartzeeTemplateSetupScreen: EmbeddedScreen()
{
    private val scrollTable = ScrollTable()
    private val panelEast = JPanel()

    init
    {
        add(scrollTable)
        add(panelEast, BorderLayout.EAST)
    }

    override fun initialise()
    {
        populateTable()
    }

    private fun populateTable()
    {
        val tm = TableUtil.DefaultModel()

        tm.addColumn("Template")
        tm.addColumn("Rules")
        tm.addColumn("Difficulty")
        tm.addColumn("Game Count")

        populateModel(tm)

        scrollTable.model = tm
    }

    private fun populateModel(tm: TableUtil.DefaultModel)
    {
        val sb = StringBuilder()
        sb.append(" SELECT t.RowId, t.Name, t.RuleCount, t.Difficulty, COUNT(g.RowId) AS GameCount")
        sb.append(" FROM DartzeeTemplate t")
        sb.append(" LEFT OUTER JOIN Game g ON (g.GameType = $GAME_TYPE_DARTZEE AND g.GameParams = t.RowId)")
        sb.append(" GROUP BY t.RowId, t.Name, t.RuleCount, t.Difficulty")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val name = rs.getString("Name")
                val ruleCount = rs.getInt("RuleCount")
                val difficulty = rs.getDouble("Difficulty")
                val gameCount = rs.getInt("GameCount")

                tm.addRow(arrayOf(name, ruleCount, difficulty, gameCount))
            }
        }
    }


    override fun getScreenName() = "Dartzee Templates"

    override fun getBackTarget() = ScreenCache.getScreen(UtilitiesScreen::class.java)
}