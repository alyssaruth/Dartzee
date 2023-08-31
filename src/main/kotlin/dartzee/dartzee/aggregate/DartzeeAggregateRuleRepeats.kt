package dartzee.dartzee.aggregate

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment

class DartzeeAggregateRuleRepeats: AbstractDartzeeAggregateRule()
{
    override fun isValidRound(segments: List<DartboardSegment>): Boolean
    {
        val nonMissGroups = segments.filterNot { it.isMiss() }.groupBy { it.score }.values
        return nonMissGroups.any { it.size > 1 }
    }

    override fun getRuleIdentifier() = "DartRepeats"
    override fun toString() = "Score repeats"

    override fun getScoringDarts(darts: List<Dart>): List<Dart>
    {
        val nonMissGroups = darts.filterNot { it.multiplier == 0 }.groupBy { it.score }.values
        return nonMissGroups.firstOrNull { it.size > 1 }.orEmpty()
    }
}