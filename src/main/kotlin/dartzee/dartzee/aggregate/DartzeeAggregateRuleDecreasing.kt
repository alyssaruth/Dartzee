package dartzee.dartzee.aggregate

import dartzee.`object`.DartboardSegment

class DartzeeAggregateRuleDecreasing: AbstractDartzeeAggregateRule()
{
    override fun isValidRound(segments: List<DartboardSegment>): Boolean
    {
        val scores = segments.map { it.getTotal() }
        return scores.distinct().size == 3 && scores.sortedDescending() == scores
    }

    override fun getRuleIdentifier() = "DartsDecreasing"
    override fun toString() = "Darts are decreasing"
}