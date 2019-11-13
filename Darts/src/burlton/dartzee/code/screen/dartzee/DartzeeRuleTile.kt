package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.DartsColour
import java.awt.Color
import java.awt.Dimension
import javax.swing.DefaultButtonModel
import javax.swing.JButton

class DartzeeRuleTile(dto: DartzeeRuleDto, ruleNumber: Int): JButton()
{
    init
    {
        preferredSize = Dimension(150, 80)
        text = "<html><center><b>#$ruleNumber <br /><br /> ${dto.generateRuleDescription()}</b></center></html>"
    }

    fun setResult(success: Boolean)
    {
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


    class SoftDisableButtonModel : DefaultButtonModel()
    {
        override fun isPressed() = false
        override fun isRollover() = false
    }
}