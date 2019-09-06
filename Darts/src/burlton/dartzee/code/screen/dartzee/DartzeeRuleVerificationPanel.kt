package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.bean.DartzeeDartResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.getValidSegments
import burlton.dartzee.code.listener.DartboardListener
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.TitledBorder

class DartzeeRuleVerificationPanel(private val parent: DartzeeRuleCreationDialog) : JPanel(), DartboardListener, ActionListener
{
    val dartboard = DartboardRuleVerifier(400, 400)
    private val dartsThrown = mutableListOf<Dart>()
    private val btnReset = JButton()
    private val panelSouth = JPanel()
    private val panelDartSummary = JPanel()
    private val panelDartOne = DartzeeDartResult()
    private val panelDartTwo = DartzeeDartResult()
    private val panelDartThree = DartzeeDartResult()

    private var dartzeeRule = DartzeeRuleDto(null, null, null, null, false, false)

    init
    {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(430, 400)
        border = TitledBorder("Test Rule")

        dartboard.renderScoreLabels = true
        dartboard.paintDartboard()
        dartboard.addDartboardListener(this)
        add(dartboard, BorderLayout.CENTER)
        add(panelSouth, BorderLayout.SOUTH)

        panelSouth.layout = BorderLayout(0, 0)

        panelSouth.add(btnReset, BorderLayout.WEST)

        panelDartSummary.layout = MigLayout("", "[]", "[]")
        panelSouth.add(panelDartSummary, BorderLayout.CENTER)

        panelDartSummary.add(panelDartOne, "cell 0 0")
        panelDartSummary.add(panelDartTwo, "cell 0 1")
        panelDartSummary.add(panelDartThree, "cell 0 2")

        btnReset.preferredSize = Dimension(80, 80)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset darts"

        btnReset.addActionListener(this)
    }

    fun updateRule(rule: DartzeeRuleDto)
    {
        this.dartzeeRule = rule

        repaintDartboard()
    }


    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)

        panelDartOne.update(dartsThrown[0])

        if (dartsThrown.size > 1)
        {
            panelDartTwo.update(dartsThrown[1])
        }

        if (dartsThrown.size > 2)
        {
            panelDartThree.update(dartsThrown[2])
        }

        if (dartsThrown.size == 3)
        {
            dartboard.stopListening()
        }

        repaintDartboard()
    }

    private fun repaintDartboard()
    {
        if (dartsThrown.size < 3)
        {
            val calculationResult = dartzeeRule.getValidSegments(dartboard, dartsThrown)

            dartboard.refreshValidSegments(calculationResult.validSegments)
            parent.updateRuleStrength(calculationResult)
        }
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        dartsThrown.clear()
        dartboard.clearDarts()
        dartboard.ensureListening()

        panelDartOne.reset()
        panelDartTwo.reset()
        panelDartThree.reset()

        repaintDartboard()
    }
}