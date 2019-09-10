package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.InjectedThings.dartzeeCalculator
import burlton.dartzee.code.utils.InjectedThings.verificationDartboardSize
import burlton.desktopcore.code.util.setFontSize
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.TitledBorder

class DartzeeRuleVerificationPanel(private val parent: DartzeeRuleCreationDialog) : JPanel(), DartboardListener, ActionListener
{
    val dartboard = DartboardRuleVerifier(verificationDartboardSize, verificationDartboardSize)
    private val dartsThrown = mutableListOf<Dart>()
    private val btnReset = JButton()
    private val panelSouth = JPanel()

    val tfResult = JTextField()

    private var dartzeeRule = DartzeeRuleDto(null, null, null, null, false, false)

    init
    {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(430, 400)
        border = TitledBorder("Test Rule")

        dartboard.renderScoreLabels = true
        dartboard.paintDartboard()
        dartboard.addDartboardListener(this)
        add(tfResult, BorderLayout.NORTH)
        add(dartboard, BorderLayout.CENTER)
        add(panelSouth, BorderLayout.SOUTH)

        val layout = FlowLayout()
        layout.alignment = FlowLayout.CENTER
        panelSouth.layout = layout

        panelSouth.add(btnReset)

        tfResult.preferredSize = Dimension(900, 50)
        tfResult.horizontalAlignment = JTextField.CENTER
        tfResult.setFontSize(24)
        tfResult.isEditable = false

        btnReset.preferredSize = Dimension(80, 80)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset darts"

        btnReset.addActionListener(this)
        btnReset.isEnabled = false
    }

    fun updateRule(rule: DartzeeRuleDto)
    {
        this.dartzeeRule = rule

        repaintDartboard()
    }


    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)

        if (dartsThrown.size == 3)
        {
            dartboard.stopListening()
        }

        repaintDartboard()
    }

    private fun repaintDartboard()
    {
        btnReset.isEnabled = dartsThrown.isNotEmpty()

        val calculationResult = dartzeeCalculator.getValidSegments(dartzeeRule, dartboard, dartsThrown)
        if (dartsThrown.size < 3)
        {
            dartboard.refreshValidSegments(calculationResult.validSegments)
        }

        parent.updateResults(calculationResult)

        val dartStrs = dartsThrown.map { it.toString() }.toMutableList()
        while (dartStrs.size < 3) {
            dartStrs.add("?")
        }

        val dartsStr = dartStrs.joinToString(" → ")
        val totalStr = "Total: ${dartsThrown.map { it.getTotal() }.sum()}"

        tfResult.text = "$dartsStr, $totalStr"

        if (calculationResult.validCombinations == 0)
        {
            tfResult.background = DartsColour.getDarkenedColour(Color.RED)
            tfResult.foreground = Color.RED
        }
        else if (dartsThrown.size == 3)
        {
            tfResult.background = DartsColour.getDarkenedColour(Color.GREEN)
            tfResult.foreground = Color.GREEN
        }
        else
        {
            tfResult.background = DartsColour.getDarkenedColour(Color.CYAN)
            tfResult.foreground = Color.CYAN
        }
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        dartsThrown.clear()
        dartboard.clearDarts()
        dartboard.ensureListening()

        repaintDartboard()
    }
}