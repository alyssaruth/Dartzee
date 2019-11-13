package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResult
import javax.swing.JPanel

class DartzeeRuleCarousel(dtos: List<DartzeeRuleDto>): JPanel()
{
    private val tiles = dtos.mapIndexed { ix, rule -> DartzeeRuleTile(rule, ix + 1) }

    init
    {
        tiles.forEach { add(it) }
    }

    fun update(results: List<DartzeeRoundResult>)
    {
        results.forEach {
            val tile = tiles[it.ruleNumber - 1]
            tile.setResult(it.success)
        }
    }
}