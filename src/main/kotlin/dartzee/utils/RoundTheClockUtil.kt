package dartzee.utils

import dartzee.core.util.getLongestStreak
import dartzee.game.ClockType
import dartzee.`object`.Dart

fun getLongestStreak(allDarts: List<Dart>, clockType: ClockType = ClockType.Standard): List<Dart> {
    val groupedByPt = allDarts.groupBy { it.participantId }.values

    val streaks =
        groupedByPt.map { darts -> darts.getLongestStreak { it.hitClockTarget(clockType) } }
    return streaks.maxByOrNull { it.size }.orEmpty()
}
