package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.db.DARTZEE_TEMPLATE
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.DartzeeTemplateEntity
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.UtilitiesScreen
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.InjectedThings
import burlton.desktopcore.code.bean.RowSelectionListener
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.bean.addKeyAction
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.TableUtil
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel

class DartzeeTemplateSetupScreen: EmbeddedScreen(), RowSelectionListener
{
    val scrollTable = ScrollTable()
    private val panelEast = JPanel()
    val btnAdd = JButton()
    val btnCopy = JButton()
    val btnDelete = JButton()

    init
    {
        add(scrollTable)
        add(panelEast, BorderLayout.EAST)

        panelEast.layout = MigLayout("al center center, wrap, gapy 20")
        panelEast.add(btnAdd)
        panelEast.add(btnCopy)
        panelEast.add(btnDelete)

        btnAdd.icon = ImageIcon(javaClass.getResource("/buttons/add.png"))
        btnAdd.preferredSize = Dimension(40, 40)

        btnCopy.icon = ImageIcon(javaClass.getResource("/buttons/copy.png"))
        btnCopy.preferredSize = Dimension(40, 40)

        btnDelete.icon = ImageIcon(javaClass.getResource("/buttons/remove.png"))
        btnDelete.preferredSize = Dimension(40, 40)

        scrollTable.font = Font(font.name, Font.PLAIN, 18)
        scrollTable.addRowSelectionListener(this)

        scrollTable.addKeyAction(KeyEvent.VK_DELETE) { if (btnDelete.isEnabled) deleteTemplate() }
        scrollTable.setHeaderFont(Font(font.name, Font.PLAIN, 20))

        btnAdd.addActionListener(this)
        btnCopy.addActionListener(this)
        btnDelete.addActionListener(this)
    }

    override fun initialise()
    {
        val tm = TableUtil.DefaultModel()

        tm.addColumn("Template")
        tm.addColumn("Rules")
        tm.addColumn("Games")

        scrollTable.model = tm
        scrollTable.setRowName("template")
        scrollTable.setRowHeight(40)
        scrollTable.setColumnWidths("200;-1;100")

        scrollTable.setRenderer(1, DartzeeTemplateRuleRenderer())

        populateTable()

        selectionChanged(scrollTable)
    }

    private fun populateTable()
    {
        val cols = DartzeeTemplateEntity().getColumnsForSelectStatement("t")

        val allRules = DartzeeRuleEntity().retrieveEntities("EntityName = '$DARTZEE_TEMPLATE'")
        val hmTemplateIdToRules = allRules.groupBy { it.entityId }

        val sb = StringBuilder()
        sb.append(" SELECT $cols, COUNT(g.RowId) AS GameCount")
        sb.append(" FROM DartzeeTemplate t")
        sb.append(" LEFT OUTER JOIN Game g ON (g.GameType = $GAME_TYPE_DARTZEE AND g.GameParams = t.RowId)")
        sb.append(" GROUP BY $cols")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next()) {
                val template = DartzeeTemplateEntity().factoryFromResultSet(rs)
                val gameCount = rs.getInt("GameCount")

                val rules = hmTemplateIdToRules[template.rowId] ?: listOf()

                addTemplateToTable(template, rules, gameCount)
            }
        }
    }
    private fun addTemplateToTable(template: DartzeeTemplateEntity?, rules: List<DartzeeRuleEntity>, gameCount: Int)
    {
        val dtos = rules.sortedBy { it.ordinal }.map { it.toDto() }
        
        template?.let { scrollTable.addRow(arrayOf(it, dtos, gameCount))}
    }

    private fun addTemplate()
    {
        val template = InjectedThings.dartzeeTemplateFactory.newTemplate()
        template?.let { initialise() }
    }

    private fun copySelectedTemplate()
    {
        val selection = getSelectedTemplate()

        val template = InjectedThings.dartzeeTemplateFactory.copyTemplate(selection)
        template?.let { initialise() }
    }

    private fun deleteTemplate()
    {
        val selection = getSelectedTemplate()

        val ans = DialogUtil.showQuestion("Are you sure you want to delete the ${selection.name} Template?")
        if (ans != JOptionPane.YES_OPTION)
        {
            return
        }

        selection.deleteFromDatabase()
        DartzeeRuleEntity().deleteForTemplate(selection.rowId)

        initialise()
    }

    private fun getSelectedTemplate(): DartzeeTemplateEntity
    {
        val rowIndex = scrollTable.selectedModelRow

        val tm = scrollTable.model
        return tm.getValueAt(rowIndex, 0) as DartzeeTemplateEntity
    }


    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAdd -> addTemplate()
            btnCopy -> copySelectedTemplate()
            btnDelete -> deleteTemplate()
            else -> super.actionPerformed(arg0)
        }
    }

    override fun selectionChanged(src: ScrollTable)
    {
        btnCopy.isEnabled = src.selectedModelRow != -1
        btnDelete.isEnabled = src.selectedModelRow != -1
    }

    override fun getScreenName() = "Dartzee Templates"

    override fun getBackTarget() = ScreenCache.getScreen(UtilitiesScreen::class.java)
}