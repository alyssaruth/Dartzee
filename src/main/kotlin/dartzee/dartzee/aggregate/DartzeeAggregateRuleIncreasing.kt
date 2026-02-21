package dartzee.dartzee.aggregate

import dartzee.`object`.DartboardSegment

class DartzeeAggregateRuleIncreasing : AbstractDartzeeAggregateRule() {
    override fun isValidRound(segments: List<DartboardSegment>): Boolean {
        val scores = segments.map { it.getTotal() }
        return scores.distinct().size == 3 && scores.sorted() == scores
    }

    override fun getRuleIdentifier() = "DartsIncreasing"

    override fun toString() = "Darts are increasing"
}
