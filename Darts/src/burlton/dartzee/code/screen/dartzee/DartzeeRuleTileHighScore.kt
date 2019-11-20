package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto

val dto = DartzeeRuleDto(null, null, null, null, inOrder = false, allowMisses = true)
class DartzeeRuleTileHighScore : DartzeeRuleTile(dto, 0)
{
    init
    {
        text = "<html><center><b>High Score</b></center></html>"
    }
}