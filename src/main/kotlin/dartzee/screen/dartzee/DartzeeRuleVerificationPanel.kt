package dartzee.screen.dartzee

import dartzee.`object`.Dart
import dartzee.core.util.setFontSize
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.listener.DartboardListener
import dartzee.utils.DartsColour
import dartzee.utils.InjectedThings.dartzeeCalculator
import dartzee.utils.InjectedThings.dartboardSize
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder


class DartzeeRuleVerificationPanel: JPanel(), DartboardListener, ActionListener
{
    val dartboard = DartzeeDartboard(dartboardSize, dartboardSize)
    val dartsThrown = mutableListOf<Dart>()
    private var dartzeeRule = DartzeeRuleDto(null, null, null, null, false, false)

    val btnReset = JButton()
    private val panelNorth = JPanel()
    val lblCombinations = JLabel()
    val tfResult = JTextField()

    init
    {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(400, 400)

        dartboard.renderScoreLabels = true
        dartboard.paintDartboard()
        dartboard.addDartboardListener(this)
        dartboard.renderDarts = true
        add(panelNorth, BorderLayout.NORTH)
        add(dartboard, BorderLayout.CENTER)
        add(lblCombinations, BorderLayout.SOUTH)

        lblCombinations.horizontalAlignment = JLabel.CENTER
        lblCombinations.border = EmptyBorder(0, 0, 5, 0)

        panelNorth.border = EmptyBorder(4, 4, 4, 4)
        panelNorth.layout = BorderLayout(0, 0)
        panelNorth.add(tfResult, BorderLayout.CENTER)
        panelNorth.add(btnReset, BorderLayout.EAST)

        tfResult.border = null
        tfResult.preferredSize = Dimension(900, 60)
        tfResult.horizontalAlignment = JTextField.CENTER
        tfResult.setFontSize(24)
        tfResult.isEditable = false

        btnReset.preferredSize = Dimension(60, 60)
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

        if (dartsThrown.size == 3)
        {
            dartboard.stopListening()
        }

        repaintDartboard()
    }

    private fun repaintDartboard()
    {
        val calculationResult = runCalculationIfNecessary()
        val failed = calculationResult.validCombinations == 0

        if (dartsThrown.size < 3)
        {
            lblCombinations.text = calculationResult.getCombinationsDesc()
            dartboard.refreshValidSegments(calculationResult.getSegmentStatus())
        }
        else
        {
            lblCombinations.text = ""
        }

        when
        {
            failed -> setAllColours(Color.RED)
            dartsThrown.size == 3 -> setAllColours(Color.GREEN)
            else -> setAllColours(Color.WHITE, DartsColour.COLOUR_PASTEL_BLUE)
        }

        updateDartDesc(failed)
    }
    private fun updateDartDesc(failed: Boolean)
    {
        val dartStrs = dartsThrown.map { it.toString() }.toMutableList()
        while (dartStrs.size < 3) {
            dartStrs.add("?")
        }

        val dartsStr = dartStrs.joinToString(" → ")
        val total = if (failed) "Total: N/A" else "Total: ${dartzeeRule.getSuccessTotal(dartsThrown)}"

        tfResult.text = "$dartsStr, $total"
    }

    private fun runCalculationIfNecessary(): DartzeeRuleCalculationResult
    {
        return if (dartsThrown.isEmpty())
        {
            dartzeeRule.calculationResult!!
        }
        else
        {
            dartzeeCalculator.getValidSegments(dartzeeRule, dartsThrown)
        }
    }

    private fun setAllColours(fg: Color, bg: Color = DartsColour.getDarkenedColour(fg))
    {
        tfResult.foreground = fg
        lblCombinations.foreground = fg

        tfResult.background = bg
        btnReset.background = DartsColour.getDarkenedColour(bg)
        btnReset.border = null
        panelNorth.background = bg
        background = bg
        lblCombinations.background = bg
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        if (dartsThrown.isNotEmpty())
        {
            dartsThrown.clear()
            dartboard.clearDarts()
            dartboard.ensureListening()

            repaintDartboard()
        }
    }
}