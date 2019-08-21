package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.utils.getColourForPointAndSegment
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JCheckBox

class DartzeeDartRuleColour: AbstractDartzeeDartRuleConfigurable(), ActionListener
{
    var black = false
    var white = false
    var green = false
    var red = false

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

    override fun isValidSegment(segment: DartboardSegment): Boolean
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

    override fun getDescription(): String
    {
        val colourList = mutableListOf<String>()
        if (red) colourList.add("R")
        if (green) colourList.add("G")
        if (black) colourList.add("B")
        if (white) colourList.add("W")

        return colourList.joinToString("/")
    }

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        rootElement.setAttribute("Black", black.toString())
        rootElement.setAttribute("White", white.toString())
        rootElement.setAttribute("Red", red.toString())
        rootElement.setAttribute("Green", green.toString())
    }

    override fun populate(rootElement: Element)
    {
        cbBlack.isSelected = rootElement.getAttribute("Black")?.toBoolean() ?: false
        cbWhite.isSelected = rootElement.getAttribute("White")?.toBoolean() ?: false
        cbRed.isSelected = rootElement.getAttribute("Red")?.toBoolean() ?: false
        cbGreen.isSelected = rootElement.getAttribute("Green")?.toBoolean() ?: false

        updateFromUi()
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        updateFromUi()
    }

    private fun updateFromUi()
    {
        black = cbBlack.isSelected
        white = cbWhite.isSelected
        green = cbGreen.isSelected
        red = cbRed.isSelected
    }

}