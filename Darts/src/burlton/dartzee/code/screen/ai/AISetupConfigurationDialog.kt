package burlton.dartzee.code.screen.ai

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.bean.SuperTextPane
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.TableUtil
import burlton.desktopcore.code.util.TableUtil.SimpleRenderer
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Dialog to specify setup darts that override defaults. Some examples:
 * - On 48, the default is to aim for 8 (D20). But you might want to override this to aim for 16 (D16).
 * - On 10, the default is to aim for D5. But if an AI is bad, you might want to override this to aim for 2.
 * - On 35, the default is to aim for 3 (D16). But you might want to aim for 19 (D8).
 */
class AISetupConfigurationDialog : SimpleDialog()
{
    private var hmScoreToSingle = mutableMapOf<Int, Dart>()

    private val info = SuperTextPane()
    private val tableScores = ScrollTable()
    private val btnAddRule = JButton("Add Rule...")
    private val btnRemove = JButton("Remove")

    init
    {
        title = "Setup Configuration"
        setSize(500, 500)
        isResizable = false
        isModal = true

        val panel = JPanel()
        contentPane.add(panel, BorderLayout.NORTH)
        panel.layout = BorderLayout(0, 0)
        info.isEditable = false
        panel.add(info)

        val panelCenter = JPanel()
        contentPane.add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = BorderLayout(0, 0)
        panelCenter.add(tableScores)

        val panelTableOptions = JPanel()
        panelCenter.add(panelTableOptions, BorderLayout.NORTH)
        panelTableOptions.add(btnAddRule)
        panelTableOptions.add(btnRemove)

        btnAddRule.addActionListener(this)
        btnRemove.addActionListener(this)
    }

    private fun init(hmScoreToSingle: HashMap<Int, Dart>)
    {
        this.hmScoreToSingle = hmScoreToSingle

        initInfo()
        buildTable(hmScoreToSingle)
    }

    private fun initInfo()
    {
        val txt = info.text
        if (!txt.isEmpty())
        {
            return
        }

        info.append("By default, the AI strategy is as follows:")
        info.append("\n\n")

        info.append(" - ", true)
        info.append("s", true, true)
        info.append(" > 60: ", true)
        info.append("Throws a scoring dart")

        info.append("\n")

        info.append(" - 40 < ", true)
        info.append("s", true, true)
        info.append(" <= 60:", true)
        info.append(" Aim for the single (")
        info.append("s", false, true)
        info.append(" - 40)")

        info.append("\n")

        info.append(" - ", true)
        info.append("s", true, true)
        info.append(" <= 40, ", true)
        info.append("s", true, true)
        info.append(" even:", true)
        info.append(" Aim for the finish")

        info.append("\n")

        info.append(" - ", true)
        info.append("s", true, true)
        info.append(" <= 40, ", true)
        info.append("s", true, true)
        info.append(" odd:", true)
        info.append(" Aim for the single that leaves the highest power of 2 remaining")
        info.append("\n\n")

        info.append("Below you can further configure the dart aimed at for any individual score.")
    }

    private fun buildTable(hmRules: Map<Int, Dart>)
    {
        val allValues = hmRules.keys

        val tm = TableUtil.DefaultModel()
        tm.addColumn("Score")
        tm.addColumn("Dart to aim for")
        tm.addColumn("Result")

        val rows = allValues.map{ arrayOf(it, hmRules[it]!!, it - hmRules[it]!!.getTotal()) }
        tm.addRows(rows)

        tableScores.model = tm
        tableScores.setRenderer(1, SimpleRenderer(SwingConstants.RIGHT, null))
        tableScores.sortBy(0, false)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnAddRule -> {
                val hmCurrentRules = mutableMapOf<Int, Dart>()
                fillHashMapFromTable(hmCurrentRules)
                NewSetupRuleDialog.addNewSetupRule(hmCurrentRules)

                buildTable(hmCurrentRules)
            }
            btnRemove -> removeScores()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun removeScores()
    {
        val rows = tableScores.selectedModelRows
        if (rows.isEmpty())
        {
            DialogUtil.showError("You must select row(s) to remove.")
            return
        }

        val hmCurrentRules = mutableMapOf<Int, Dart>()
        fillHashMapFromTable(hmCurrentRules)

        rows.forEach{ hmCurrentRules.remove(tableScores.getValueAt(it, 0)) }

        buildTable(hmCurrentRules)
    }

    override fun okPressed()
    {
        hmScoreToSingle.clear()
        fillHashMapFromTable(hmScoreToSingle)

        dispose()
    }

    private fun fillHashMapFromTable(hm: MutableMap<Int, Dart>)
    {
        val tm = tableScores.model
        val rows = tm.rowCount
        for (i in 0 until rows)
        {
            val score = tm.getValueAt(i, 0) as Int
            val drt = tm.getValueAt(i, 1) as Dart

            hm[score] = drt
        }
    }

    companion object
    {
        @JvmStatic fun configureSetups(hmScoreToSingle: HashMap<Int, Dart>)
        {
            val dlg = AISetupConfigurationDialog()
            dlg.setLocationRelativeTo(ScreenCache.getMainScreen())
            dlg.init(hmScoreToSingle)
            dlg.isVisible = true
        }
    }
}
