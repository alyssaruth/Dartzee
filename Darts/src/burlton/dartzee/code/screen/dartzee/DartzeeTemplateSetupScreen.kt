package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.UtilitiesScreen
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class DartzeeTemplateSetupScreen: EmbeddedScreen()
{
    val scrollTable = ScrollTable()
    private val panelEast = JPanel()
    val btnAdd = JButton()

    init
    {
        add(scrollTable)
        add(panelEast, BorderLayout.EAST)

        panelEast.layout = MigLayout("al center center, wrap, gapy 20")
        panelEast.add(btnAdd)

        btnAdd.icon = ImageIcon(javaClass.getResource("/buttons/add.png"))
        btnAdd.preferredSize = Dimension(40, 40)

        btnAdd.addActionListener(this)
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
        scrollTable.setRowName("template")
        scrollTable.setRowHeight(40)
    }

    private fun populateModel(tm: TableUtil.DefaultModel)
    {
        val sb = StringBuilder()
        sb.append(" SELECT t.RowId, t.Name, t.RuleCount, t.Difficulty, COUNT(g.RowId) AS GameCount")
        sb.append(" FROM DartzeeTemplate t")
        sb.append(" LEFT OUTER JOIN Game g ON (g.GameType = $GAME_TYPE_DARTZEE AND g.GameParams = t.RowId)")
        sb.append(" GROUP BY t.RowId, t.Name, t.RuleCount, t.Difficulty")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next()) {
                val name = rs.getString("Name")
                val ruleCount = rs.getInt("RuleCount")
                val difficulty = rs.getDouble("Difficulty")
                val gameCount = rs.getInt("GameCount")

                tm.addRow(arrayOf(name, ruleCount, difficulty, gameCount))
            }
        }
    }

    private fun addTemplate()
    {
        val template = DartzeeTemplateDialog.createTemplate()
        if (template != null)
        {
            scrollTable.addRow(arrayOf(template.name, template.ruleCount, template.difficulty, 0))
        }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAdd -> addTemplate()
            else -> super.actionPerformed(arg0)
        }
    }

    override fun getScreenName() = "Dartzee Templates"

    override fun getBackTarget() = ScreenCache.getScreen(UtilitiesScreen::class.java)
}