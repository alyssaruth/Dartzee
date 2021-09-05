package dartzee.dartzee.aggregate

import dartzee.`object`.DartboardSegment

class DartzeeAggregateRuleDistinctScores: AbstractDartzeeAggregateRule()
{
    override fun isValidRound(segments: List<DartboardSegment>): Boolean
    {
        if (segments.any { it.isMiss() })
        {
            return false
        }

        return segments.distinctBy { it.score }.size == 3
    }

    override fun getRuleIdentifier() = "DistinctScores"
    override fun toString() = "Darts are distinct"
}