package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.DartzeeRuleCalculationResult
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.InjectedThings.dartzeeCalculator
import burlton.dartzee.code.utils.InjectedThings.verificationDartboardSize
import burlton.desktopcore.code.util.setFontSize
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder


class DartzeeRuleVerificationPanel: JPanel(), DartboardListener, ActionListener
{
    private val bgColor = DartsColour.COLOUR_PASTEL_BLUE

    val dartboard = DartboardRuleVerifier(verificationDartboardSize, verificationDartboardSize)
    private val dartsThrown = mutableListOf<Dart>()
    val btnReset = JButton()
    private val panelNorth = JPanel()
    private val lblCombinations = JLabel()

    val tfResult = JTextField()

    private var dartzeeRule = DartzeeRuleDto(null, null, null, null, false, false)

    init
    {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(400, 400)
        background = bgColor
        panelNorth.background = bgColor
        dartboard.background = bgColor

        dartboard.renderScoreLabels = true
        dartboard.paintDartboard()
        dartboard.addDartboardListener(this)
        add(panelNorth, BorderLayout.NORTH)
        add(dartboard, BorderLayout.CENTER)
        add(lblCombinations, BorderLayout.SOUTH)

        lblCombinations.horizontalAlignment = JLabel.CENTER

        btnReset.background = DartsColour.getDarkenedColour(bgColor)

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

        updateDartDesc()

        if (dartsThrown.size < 3)
        {
            lblCombinations.text = calculationResult.getCombinationsDesc()
            dartboard.refreshValidSegments(calculationResult.validSegments)

            if (calculationResult.validCombinations == 0)
            {
                //We've already borked it
                setAllColours(Color.RED)

            }
            else
            {
                setAllColours(Color.WHITE, bgColor)
            }
        }
        else
        {
            //We've thrown three darts, so just check validity
            lblCombinations.text = ""

            val combination = dartsThrown.map { dartboard.getSegment(it.score, it.segmentType)!! }
            if (dartzeeCalculator.isValidCombination(combination, dartzeeRule))
            {
                setAllColours(Color.GREEN)
            }
            else
            {
                setAllColours(Color.RED)
            }
        }
    }
    private fun updateDartDesc()
    {
        val dartStrs = dartsThrown.map { it.toString() }.toMutableList()
        while (dartStrs.size < 3) {
            dartStrs.add("?")
        }

        val dartsStr = dartStrs.joinToString(" → ")
        val totalStr = "Total: ${dartsThrown.map { it.getTotal() }.sum()}"

        tfResult.text = "$dartsStr, $totalStr"
    }

    private fun runCalculationIfNecessary(): DartzeeRuleCalculationResult
    {
        return if (dartsThrown.isEmpty())
        {
            dartzeeRule.calculationResult!!
        }
        else
        {
            dartzeeCalculator.getValidSegments(dartzeeRule, dartboard, dartsThrown)
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