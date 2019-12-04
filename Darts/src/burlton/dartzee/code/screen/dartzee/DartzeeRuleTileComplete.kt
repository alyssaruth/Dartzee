package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.setColoursForDartzeeResult
import burlton.desktopcore.code.util.setFontSize
import java.awt.event.MouseEvent
import javax.swing.DefaultButtonModel

class DartzeeRuleTileComplete(dto: DartzeeRuleDto, ruleNumber: Int, success: Boolean, val score: Int): DartzeeRuleTile(dto, ruleNumber)
{
    init
    {
        model = SoftDisableButtonModel()
        isFocusable = false

        setColoursForDartzeeResult(success)
    }

    override fun getScore() = score

    override fun mouseEntered(e: MouseEvent?)
    {
        text = getButtonText(true)
        setFontSize(24)
    }

    override fun mouseExited(e: MouseEvent?)
    {
        text = getButtonText()
        setFontSize(12)
    }
}

class SoftDisableButtonModel : DefaultButtonModel()
{
    override fun isPressed() = false
    override fun isRollover() = false
}