package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.InjectedThings
import java.awt.Color
import java.awt.Dimension
import javax.swing.DefaultButtonModel
import javax.swing.JButton

class DartzeeRuleTile(val dto: DartzeeRuleDto, ruleNumber: Int): JButton()
{
    var result: Boolean? = null

    init
    {
        preferredSize = Dimension(150, 80)
        text = "<html><center><b>#$ruleNumber <br /><br /> ${dto.generateRuleDescription()}</b></center></html>"
    }

    fun setResult(success: Boolean)
    {
        result = success
        model = SoftDisableButtonModel()
        isFocusable = false

        if (success)
        {
            background = Color.GREEN
            foreground = DartsColour.getProportionalColour(1.0, 1, 0.4, 0.5)
        }
        else
        {
            background = Color.RED
            foreground = DartsColour.getProportionalColour(0.0, 1, 0.4, 0.5)
        }
    }

    fun updateState(dartboard: Dartboard, darts: List<Dart>)
    {
        if (darts.isEmpty())
        {
            isEnabled = true
        }
        else if (result == null)
        {
            isEnabled = getValidSegments(darts).isNotEmpty()
        }
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