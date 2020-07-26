package dartzee.utils

import dartzee.`object`.Dart
import dartzee.ai.DartsAiModel

fun isBust(score: Int, lastDart: Dart): Boolean
{
    return (score < 0
            || score == 1
            || score == 0 && !lastDart.isDouble())
}

/**
 * Apply the Mercy Rule if:
 * - It has been enabled for this AI
 * - The starting score was odd and < the threshold (configurable per AI)
 * - The current score is even, meaing we have bailed ourselves out in some way
 */
fun shouldStopForMercyRule(model: DartsAiModel, startingScore: Int, currentScore: Int): Boolean
{
    val mercyThreshold = model.mercyThreshold
    return if (mercyThreshold == -1) {
        false
    } else startingScore < mercyThreshold
            && startingScore % 2 != 0
            && currentScore % 2 == 0

}

/**
 * 50, 40, 38, 36, 34, ... , 8, 4, 2
 */
fun isCheckoutDart(drt: Dart): Boolean
{
    val startingScore = drt.startingScore
    return isCheckoutScore(startingScore)
}
fun isCheckoutScore(score: Int): Boolean
{
    return getCheckoutScores().contains(score)
}

fun isNearMissDouble(dart: Dart): Boolean
{
    if (!isCheckoutDart(dart))
    {
        return false
    }

    //Outer bull case
    if (dart.startingScore == 50)
    {
        return dart.score == 25 && dart.multiplier == 1
    }

    val adjacents = getAdjacentNumbers(dart.startingScore/2)

    return dart.multiplier == 2
      && adjacents.contains(dart.score)
}

fun getCheckoutScores(): MutableList<Int>
{
    val list = mutableListOf(50)
    for (i in 2..40 step 2)
    {
        list.add(i)
    }

    return list
}
fun getCheckoutSingles(): List<Int> = getCheckoutScores().map { it / 2 }

fun isFinishRound(round: List<Dart>): Boolean
{
    val drt = round.last()
    return drt.isDouble() && drt.getTotal() == drt.startingScore
}

/**
 * Refactored out of GameWrapper for use in game stats panel
 */
fun getScoringDarts(allDarts: List<Dart>?, scoreCutOff: Int): MutableList<Dart>
{
    allDarts ?: return mutableListOf()

    return allDarts.filter { it.startingScore > scoreCutOff }.toMutableList()
}

fun calculateThreeDartAverage(darts: List<Dart>, scoreCutOff: Int): Double
{
    val scoringDarts = getScoringDarts(darts, scoreCutOff)
    if (scoringDarts.isEmpty())
    {
        return -1.0
    }

    val amountScored = sumScore(scoringDarts).toDouble()

    return amountScored / scoringDarts.size * 3
}

fun sumScore(darts: List<Dart>): Int
{
    return darts.map { it.getTotal() }.sum()
}

/**
 * Shanghai: T20, D20, 20 in any order.
 *
 * Check there are 3 darts, all are 20s, the sum is 120 and there is at least one single
 */
fun isShanghai(darts: MutableList<Dart>): Boolean
{
    return darts.size == 3
      && sumScore(darts) == 120
      && darts.all { it.score == 20 }
      && darts.any { it.multiplier == 1 }
}

/**
 * Sorts the array of darts deterministically and then returns a String representation.
 *
 * (5, T20, 1) -> "T20, 5, 1".
 */
fun getSortedDartStr(darts: List<Dart>): String
{
    val sortedDarts = darts.sortedWith(compareByDescending<Dart>{ it.getTotal() }.thenByDescending{ it.multiplier })
    return sortedDarts.joinToString { it.getRendered() }
}
