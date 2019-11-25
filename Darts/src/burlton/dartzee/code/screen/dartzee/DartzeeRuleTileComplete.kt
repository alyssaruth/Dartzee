package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.setColoursForDartzeeResult
import javax.swing.DefaultButtonModel

class DartzeeRuleTileComplete(dto: DartzeeRuleDto, ruleNumber: Int, val success: Boolean): DartzeeRuleTile(dto, ruleNumber)
{
    init
    {
        model = SoftDisableButtonModel()
        isFocusable = false

        setColoursForDartzeeResult(success)
    }

    class SoftDisableButtonModel : DefaultButtonModel()
    {
        override fun isPressed() = false
        override fun isRollover() = false
    }
}