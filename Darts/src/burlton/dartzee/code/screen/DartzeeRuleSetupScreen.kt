package burlton.dartzee.code.screen

import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.bean.RowSelectionListener
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JButton

class DartzeeRuleSetupScreen : EmbeddedScreen(), RowSelectionListener
{
    private val tm = TableUtil.DefaultModel()

    private val tableRules = ScrollTable()
    private val btnAddRule = JButton("+")
    private val btnAmendRule = JButton("#")
    private val btnCalculateOrder = JButton("Calc")

    init
    {
        add(tableRules, BorderLayout.CENTER)
        tableRules.enableManualReordering()

        tableRules.addButtonToOrderingPanel(btnAddRule, 0)
        tableRules.addButtonToOrderingPanel(btnAmendRule, 1)
        tableRules.addButtonToOrderingPanel(btnCalculateOrder, 5)

        tableRules.setRowName("rule")

        tableRules.addRowSelectionListener(this)

        btnAddRule.addActionListener(this)
        btnAmendRule.addActionListener(this)
        btnCalculateOrder.addActionListener(this)
    }

    override fun initialise()
    {
        setTableModel()
    }

    private fun setTableModel()
    {
        tm.addColumn("Rule")
        tm.addColumn("Description")

        tableRules.model = tm

        btnAmendRule.isEnabled = false
    }

    fun setState(match: DartsMatchEntity?, players: MutableList<PlayerEntity>)
    {

    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAddRule -> addRule()
            btnAmendRule -> ""
            btnCalculateOrder -> ""
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
    private fun addRuleToTable(rule: DartzeeRuleEntity)
    {
        tm.addRow(arrayOf(rule.dart1Rule?.toDbString(), rule.dart2Rule?.toDbString()))
    }


    override fun selectionChanged(src: ScrollTable)
    {
        btnAmendRule.isEnabled = src.selectedModelRow != -1
    }

    override fun getScreenName(): String
    {
        return "Dartzee Setup"
    }

    override fun getBackTarget(): EmbeddedScreen
    {
        return ScreenCache.getScreen(GameSetupScreen::class.java)
    }
}