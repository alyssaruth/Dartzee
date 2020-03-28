package dartzee.utils

import dartzee.`object`.Dart
import dartzee.core.util.getLongestStreak
import dartzee.db.CLOCK_TYPE_STANDARD

fun getLongestStreak(allDarts: List<Dart>, gameParams: String = CLOCK_TYPE_STANDARD): List<Dart>
{
    val groupedByPt = allDarts.groupBy { it.participantId }.values

    val streaks = groupedByPt.map { darts -> darts.getLongestStreak { it.hitClockTarget(gameParams) } }
    return streaks.maxBy { it.size } ?: listOf()
}