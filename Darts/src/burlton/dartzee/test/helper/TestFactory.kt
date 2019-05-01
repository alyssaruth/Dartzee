package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.Dart

fun factoryClockHit(score: Int, multiplier: Int = 1): Dart
{
    val dart = Dart(score, multiplier)
    dart.startingScore = score
    return dart
}