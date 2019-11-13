package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import javax.swing.JPanel

class DartzeeRuleCarousel(dtos: List<DartzeeRuleDto>): JPanel()
{
    init
    {
        dtos.forEachIndexed { ix, rule -> add(DartzeeRuleTile(rule, ix + 1)) }
    }
}