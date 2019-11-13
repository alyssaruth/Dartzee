package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import java.awt.Color
import java.awt.Dimension
import javax.swing.JButton

class DartzeeRuleTile(dto: DartzeeRuleDto, ruleNumber: Int): JButton()
{
    init
    {
        preferredSize = Dimension(150, 80)
        text = "<html><center>#$ruleNumber <br /><br /> ${dto.generateRuleDescription()}</center></html>"
    }

    fun setResult(success: Boolean)
    {
        isEnabled = false

        if (success)
        {
            background = Color.GREEN
            foreground = Color.GREEN
        }
        else
        {
            background = Color.RED
            foreground = Color.RED
        }
    }
}