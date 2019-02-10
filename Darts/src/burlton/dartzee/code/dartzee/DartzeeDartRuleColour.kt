package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.utils.getColourForPointAndSegment
import org.w3c.dom.Element
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JPanel

class DartzeeDartRuleColour: AbstractDartzeeDartRule(), ActionListener
{
    var black = false
    var white = false
    var green = false
    var red = false

    private val configPanel = JPanel()
    private val cbBlack = JCheckBox("Black")
    private val cbWhite = JCheckBox("White")
    private val cbGreen = JCheckBox("Green")
    private val cbRed = JCheckBox("Red")

    init
    {
        configPanel.layout = FlowLayout()

        cbBlack.name = "Black"
        cbWhite.name = "White"
        cbGreen.name = "Green"
        cbRed.name = "Red"

        configPanel.add(cbBlack)
        configPanel.add(cbWhite)
        configPanel.add(cbRed)
        configPanel.add(cbGreen)

        cbBlack.addActionListener(this)
        cbWhite.addActionListener(this)
        cbGreen.addActionListener(this)
        cbRed.addActionListener(this)
    }

    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        if (segment.isMiss())
        {
            return false
        }

        val color = getColourForPointAndSegment(null, segment, false, DEFAULT_COLOUR_WRAPPER)

        return (color == Color.BLACK && black)
                || (color == Color.WHITE && white)
                || (color == Color.GREEN && green)
                || (color == Color.RED && red)
    }

    override fun getRuleIdentifier() = "Colour"
    override fun validate(): String
    {
        if (!red && !green && !black && !white)
        {
            return "You must select at least one colour."
        }

        return ""
    }

    override fun writeXmlAttributes(rootElement: Element)
    {
        rootElement.setAttribute("Black", black.toString())
        rootElement.setAttribute("White", white.toString())
        rootElement.setAttribute("Red", red.toString())
        rootElement.setAttribute("Green", green.toString())
    }

    override fun populate(rootElement: Element)
    {
        black = rootElement.getAttribute("Black")?.toBoolean() ?: false
        white = rootElement.getAttribute("White")?.toBoolean() ?: false
        red = rootElement.getAttribute("Red")?.toBoolean() ?: false
        green = rootElement.getAttribute("Green")?.toBoolean() ?: false
    }

    override fun getConfigPanel() = configPanel
    override fun actionPerformed(e: ActionEvent?)
    {
        black = cbBlack.isSelected
        white = cbWhite.isSelected
        green = cbGreen.isSelected
        red = cbRed.isSelected
    }
}