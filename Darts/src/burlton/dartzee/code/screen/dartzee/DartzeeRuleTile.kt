package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.code.utils.setColoursForDartzeeResult
import org.jfree.chart.imagemap.ImageMapUtilities
import java.awt.Dimension
import javax.swing.DefaultButtonModel
import javax.swing.JButton

open class DartzeeRuleTile(val dto: DartzeeRuleDto, val ruleNumber: Int): JButton()
{
    var result: Boolean? = null
    var pendingResult: Boolean? = null

    init
    {
        val ruleDesc = ImageMapUtilities.htmlEscape(dto.generateRuleDescription())
        preferredSize = Dimension(150, 80)
        text = "<html><center><b>#$ruleNumber <br /><br /> $ruleDesc</b></center></html>"
    }

    fun clearPendingResult()
    {
        pendingResult = null
        isFocusable = true

        background = null
        foreground = null
    }

    fun setPendingResult(success: Boolean)
    {
        pendingResult = success
        isFocusable = false

        setColoursForDartzeeResult(success)
    }

    fun setResult(success: Boolean)
    {
        result = success
        model = SoftDisableButtonModel()
        isFocusable = false

        setColoursForDartzeeResult(success)
    }

    fun updateState(darts: List<Dart>)
    {
        isVisible = getValidSegments(darts).isNotEmpty()
    }

    fun getValidSegments(darts: List<Dart>): List<DartboardSegment>
    {
        if (result != null)
        {
            return listOf()
        }

        if (darts.isEmpty())
        {
            return dto.calculationResult!!.validSegments
        }
        else
        {
            val result = InjectedThings.dartzeeCalculator.getValidSegments(dto, darts)
            return result.validSegments
        }
    }

    class SoftDisableButtonModel : DefaultButtonModel()
    {
        override fun isPressed() = false
        override fun isRollover() = false
    }
}