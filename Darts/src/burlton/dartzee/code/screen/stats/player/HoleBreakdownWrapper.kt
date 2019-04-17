package burlton.dartzee.code.screen.stats.player

import burlton.core.code.obj.HashMapCount

class HoleBreakdownWrapper
{
    private val hmScoreToCount = HashMapCount<Int>()

    fun getAverage(): Double
    {
        val totalGamesCounted = hmScoreToCount.getTotalCount().toDouble()

        var weightedTotal = 0.0
        val scores = hmScoreToCount.getKeysAsVector()
        for (score in scores)
        {
            val count = hmScoreToCount.getCount(score)
            weightedTotal += (score * count).toDouble()
        }

        return weightedTotal / totalGamesCounted
    }

    fun increment(score: Int) = hmScoreToCount.incrementCount(score)
    fun getCount(score: Int) = hmScoreToCount.getCount(score)
    fun getAsTableRow(holeIdentifier: Any) = arrayOf(holeIdentifier, getCount(1), getCount(2), getCount(3), getCount(4), getCount(5), getAverage())
}