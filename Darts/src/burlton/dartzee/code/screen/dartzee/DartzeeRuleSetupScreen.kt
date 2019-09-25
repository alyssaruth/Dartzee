package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.GameSetupScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.AbstractTableRenderer
import burlton.desktopcore.code.bean.RowSelectionListener
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton

class DartzeeRuleSetupScreen : EmbeddedScreen(), RowSelectionListener
{
    private val tm = TableUtil.DefaultModel()

    private val tableRules = ScrollTable()
    private val btnAddRule = JButton()
    private val btnAmendRule = JButton()
    private val btnRemoveRule = JButton()
    private val btnCalculateOrder = JButton("Calc")

    init
    {
        add(tableRules, BorderLayout.CENTER)
        tableRules.enableManualReordering()

        tableRules.addButtonToOrderingPanel(btnAddRule, 0)
        tableRules.addButtonToOrderingPanel(btnAmendRule, 1)
        tableRules.addButtonToOrderingPanel(btnRemoveRule, 2)
        tableRules.addButtonToOrderingPanel(btnCalculateOrder, 6)

        tableRules.setRowName("rule")
        tableRules.setRowHeight(40)
        tableRules.addRowSelectionListener(this)

        btnAddRule.icon = ImageIcon(javaClass.getResource("/buttons/add.png"))
        btnAddRule.toolTipText = "Add rule"
        btnAddRule.preferredSize = Dimension(40, 40)

        btnAmendRule.icon = ImageIcon(javaClass.getResource("/buttons/amend.png"))
        btnAmendRule.toolTipText = "Edit rule"
        btnAmendRule.preferredSize = Dimension(40, 40)

        btnRemoveRule.icon = ImageIcon(javaClass.getResource("/buttons/remove.png"))
        btnRemoveRule.toolTipText = "Remove rule"
        btnRemoveRule.preferredSize = Dimension(40, 40)

        btnAddRule.addActionListener(this)
        btnAmendRule.addActionListener(this)
        btnRemoveRule.addActionListener(this)
        btnCalculateOrder.addActionListener(this)
    }

    override fun initialise()
    {
        setTableModel()
    }

    private fun setTableModel()
    {
        tm.addColumn("Rule")
        tm.addColumn("Difficulty")

        tableRules.model = tm

        tableRules.setRenderer(0, DartzeeRuleRenderer(0))
        tableRules.setRenderer(1, DartzeeRuleRenderer(1))

        selectionChanged(tableRules)
    }

    fun setState(match: DartsMatchEntity?, players: MutableList<PlayerEntity>)
    {

    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAddRule -> addRule()
            btnAmendRule -> amendRule()
            btnRemoveRule -> removeRule()
            btnCalculateOrder -> sortRulesByDifficulty()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun addRule()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.isVisible = true

        val rule = dlg.dartzeeRule
        if (rule != null)
        {
            addRuleToTable(rule)
        }
    }
    private fun amendRule()
    {
        val selection = tm.getValueAt(tableRules.selectedModelRow, 0) as DartzeeRuleDto
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(selection)
        dlg.isVisible = true

        tableRules.repaint()
    }
    private fun removeRule()
    {
        tm.removeRow(tableRules.selectedModelRow)

        tableRules.repaint()
    }
    private fun sortRulesByDifficulty()
    {
        val comparator = compareBy<Array<Any>> { -(it[0] as DartzeeRuleDto).getDifficulty() }
        tableRules.reorderRows(comparator)
    }

    private fun addRuleToTable(rule: DartzeeRuleDto)
    {
        tableRules.addRow(arrayOf(rule, rule))
    }

    override fun selectionChanged(src: ScrollTable)
    {
        btnAmendRule.isEnabled = src.selectedModelRow != -1
        btnRemoveRule.isEnabled = src.selectedModelRow != -1
    }

    override fun getScreenName() = "Dartzee Setup"
    override fun getBackTarget() = ScreenCache.getScreen(GameSetupScreen::class.java)

    /**
     * Inner classes
     */
    private inner class DartzeeRuleRenderer(private val colNo: Int) : AbstractTableRenderer<DartzeeRuleDto>()
    {
        override fun getReplacementValue(value: DartzeeRuleDto): Any
        {
            return if (colNo == 0) value.generateRuleDescription() else value.getDifficultyDesc()
        }

        override fun setCellColours(typedValue: DartzeeRuleDto?, isSelected: Boolean)
        {
            font = Font(font.name, Font.PLAIN, 20)
        }
    }
}