package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.utils.getColourForPointAndSegment
import burlton.desktopcore.code.util.DialogUtil
import org.w3c.dom.Element
import java.awt.Color

class DartzeeDartRuleColour: AbstractDartzeeDartRule()
{
    private var black = false
    private var white = false
    private var green = false
    private var red = false

    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        val color = getColourForPointAndSegment(null, segment, false, DEFAULT_COLOUR_WRAPPER)

        return (color == Color.BLACK && black)
                || (color == Color.WHITE && white)
                || (color == Color.GREEN && green)
                || (color == Color.RED && red)
    }

    override fun getRuleIdentifier() = "Colour"
    override fun isValid(): Boolean
    {
        if (!red && !green && !black && !white)
        {
            DialogUtil.showError("You must select at least one colour.")
            return false
        }

        return true
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

}