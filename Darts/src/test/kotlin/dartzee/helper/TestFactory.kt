package dartzee.test.helper

import dartzee.`object`.Dart
import java.awt.Point

fun factoryClockHit(score: Int, multiplier: Int = 1): Dart
{
    val dart = Dart(score, multiplier)
    dart.startingScore = score
    return dart
}

fun makeDart(score: Int, multiplier: Int, segmentType: Int): Dart
{
    val dart = Dart(score, multiplier)
    dart.segmentType = segmentType
    dart.pt = Point(0, 0)
    return dart
}