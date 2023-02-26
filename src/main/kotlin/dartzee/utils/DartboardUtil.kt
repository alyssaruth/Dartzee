package dartzee.utils

import dartzee.ai.AI_DARTBOARD
import dartzee.`object`.ColourWrapper
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.Dartboard
import java.awt.Color
import java.awt.Point

/**
 * Utilities for the Dartboard object.
 */
private val numberOrder = mutableListOf(20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5, 20)

val hmScoreToOrdinal = initialiseOrdinalHashMap()
private var colourWrapperFromPrefs: ColourWrapper? = null

private const val RATIO_INNER_BULL = 0.038
private const val RATIO_OUTER_BULL = 0.094
private const val LOWER_BOUND_TRIPLE_RATIO = 0.582
private const val UPPER_BOUND_TRIPLE_RATIO = 0.629
private const val LOWER_BOUND_DOUBLE_RATIO = 0.953

private const val UPPER_BOUND_DOUBLE_RATIO = 1.0
const val UPPER_BOUND_OUTSIDE_BOARD_RATIO = 1.3

fun getDartForSegment(pt: Point, segment: DartboardSegment): Dart
{
    val score = segment.score
    val multiplier = segment.getMultiplier()
    return Dart(score, multiplier, pt, segment.type)
}

fun getNumbersWithinN(number: Int, n: Int): List<Int>
{
    val ix = numberOrder.indexOf(number)
    val range = ((ix-n)..(ix+n))

    return range.map { numberOrder[(it+20) % 20] }
}

fun getAdjacentNumbers(number: Int): List<Int>
{
    return getNumbersWithinN(number, 1).filterNot { it == number }
}

fun computePointsForSegment(segment: DartboardSegment, centre: Point, diameter: Double): Set<Point>
{
    if (segment.isMiss()) {
        return emptySet()
    }

    val score = segment.score
    if (score == 25) {
        return emptySet()
    } else {
        val (startAngle, endAngle) = getAnglesForScore(score)
        val (lowerRadius, upperRadius) = getRadiiForSegmentType(segment.type, diameter)
        return (startAngle * 10 until endAngle * 10).flatMap { angle ->
            val actualAngle = angle.toDouble() / 10
            ((10 * lowerRadius).toInt() until (10 * upperRadius).toInt()).map { r ->
                translatePoint(centre, r.toDouble() / 10, actualAngle)
            }
        }.toSet()
    }
}

fun computeEdgePoints(segmentPoints: Set<Point>): Set<Point>
{
    val ptsByX = segmentPoints.groupBy { it.x }
    val ptsByY = segmentPoints.groupBy { it.y }

    val yMins: List<Point> = ptsByX.values.map { points -> points.minByOrNull { it.y }!! }
    val yMaxes: List<Point> = ptsByX.values.map { points -> points.maxByOrNull { it.y }!! }
    val xMins: List<Point> = ptsByY.values.map { points -> points.minByOrNull { it.x }!! }
    val xMaxes: List<Point> = ptsByY.values.map { points -> points.maxByOrNull { it.x }!! }

    return (yMins + yMaxes + xMins + xMaxes).toSet()
}

fun getAnglesForScore(score: Int): Pair<Int, Int> {
    val scoreIndex = numberOrder.indexOf(score) - 1
    val startAngle = 9 + (18 * scoreIndex)
    val endAngle = 9 + (18 * (scoreIndex + 1))

    return Pair(startAngle, endAngle)
}

fun getRadiiForSegmentType(segmentType: SegmentType, diameter: Double): Pair<Double, Double> {
    val radius = diameter / 2
    val (lowerRatio, upperRatio) = getRatioBounds(segmentType)
    return Pair((radius * lowerRatio), (radius * upperRatio))
}
private fun getRatioBounds(segmentType: SegmentType): Pair<Double, Double> {
    return when (segmentType) {
        SegmentType.INNER_SINGLE -> Pair(RATIO_OUTER_BULL, LOWER_BOUND_TRIPLE_RATIO)
        SegmentType.TREBLE -> Pair(LOWER_BOUND_TRIPLE_RATIO, UPPER_BOUND_TRIPLE_RATIO)
        SegmentType.OUTER_SINGLE -> Pair(UPPER_BOUND_TRIPLE_RATIO, LOWER_BOUND_DOUBLE_RATIO)
        SegmentType.DOUBLE -> Pair(LOWER_BOUND_DOUBLE_RATIO, UPPER_BOUND_DOUBLE_RATIO)
        else -> throw IllegalArgumentException("Invalid segment type: $segmentType")
    }
}

fun factorySegmentForPoint(dartPt: Point, centerPt: Point, diameter: Double): DartboardSegment
{
    val radius = getDistance(dartPt, centerPt)
    val ratio = 2 * radius / diameter

    if (ratio < RATIO_INNER_BULL)
    {
        return DartboardSegment(SegmentType.DOUBLE, 25)
    }
    else if (ratio < RATIO_OUTER_BULL)
    {
        return DartboardSegment(SegmentType.OUTER_SINGLE, 25)
    }

    //We've not hit the bullseye, so do other calculations to work out score/multiplier
    val angle = getAngleForPoint(dartPt, centerPt)
    val score = getScoreForAngle(angle)
    val type = calculateTypeForRatioNonBullseye(ratio)

    return DartboardSegment(type, score)
}

fun convertForUiDartboard(sourcePt: Point, destinationDartboard: Dartboard): Point =
    convertForDestinationDartboard(sourcePt, AI_DARTBOARD, destinationDartboard)

fun convertForDestinationDartboard(sourcePt: Point, sourceDartboard: Dartboard, destinationDartboard: Dartboard): Point =
    convertForDestinationDartboard(sourcePt, sourceDartboard.centerPoint, sourceDartboard.diameter, destinationDartboard)

fun convertForDestinationDartboard(sourcePt: Point, oldCenter: Point, oldDiameter: Double, destinationDartboard: Dartboard): Point
{
    val relativeDistance = getDistance(sourcePt, oldCenter) / oldDiameter
    val angle = getAngleForPoint(sourcePt, oldCenter)

    val newPoint = translatePoint(destinationDartboard.centerPoint, relativeDistance * destinationDartboard.diameter, angle)
    destinationDartboard.rationalisePoint(newPoint)

    val desiredSegment = factorySegmentForPoint(sourcePt, oldCenter, oldDiameter)
    val candidatePoints = mutableSetOf(newPoint)
    while (candidatePoints.none { destinationDartboard.getDataSegmentForPoint(it) == desiredSegment })
    {
        val neighbours = candidatePoints.flatMap(::getNeighbours)
        neighbours.forEach(destinationDartboard::rationalisePoint)
        candidatePoints.addAll(neighbours)
    }

    return candidatePoints.first { destinationDartboard.getDataSegmentForPoint(it) == desiredSegment }
}

/**
 * 1) Calculate the radius from the center to our point
 * 2) Using the diameter, work out whether this makes us a miss, single, double or treble
 */
private fun calculateTypeForRatioNonBullseye(ratioToDiameter: Double) =
    when
    {
        ratioToDiameter < LOWER_BOUND_TRIPLE_RATIO -> SegmentType.INNER_SINGLE
        ratioToDiameter < UPPER_BOUND_TRIPLE_RATIO -> SegmentType.TREBLE
        ratioToDiameter < LOWER_BOUND_DOUBLE_RATIO -> SegmentType.OUTER_SINGLE
        ratioToDiameter < UPPER_BOUND_DOUBLE_RATIO -> SegmentType.DOUBLE
        ratioToDiameter < UPPER_BOUND_OUTSIDE_BOARD_RATIO -> SegmentType.MISS
        else -> SegmentType.MISSED_BOARD
    }

private fun getScoreForAngle(angle: Double): Int
{
    var checkValue = 9
    var index = 0
    while (angle > checkValue)
    {
        index++
        checkValue += 18
    }

    return numberOrder[index]
}

data class AimPoint(val centerPoint: Point, val radius: Double, val angle: Int, val ratio: Double)
{
    val point = translatePoint(centerPoint, radius * ratio, angle.toDouble())
}
fun getPotentialAimPoints(centerPt: Point, diameter: Double): Set<AimPoint>
{
    val radius = diameter / 2

    val points = mutableSetOf<AimPoint>()
    for (angle in 0 until 360 step 9)
    {
        points.add(AimPoint(centerPt, radius, angle, (RATIO_OUTER_BULL + LOWER_BOUND_TRIPLE_RATIO)/2))
        points.add(AimPoint(centerPt, radius, angle, (LOWER_BOUND_TRIPLE_RATIO + UPPER_BOUND_TRIPLE_RATIO)/2))
        points.add(AimPoint(centerPt, radius, angle, (UPPER_BOUND_TRIPLE_RATIO + LOWER_BOUND_DOUBLE_RATIO)/2))
        points.add(AimPoint(centerPt, radius, angle, (LOWER_BOUND_DOUBLE_RATIO + UPPER_BOUND_DOUBLE_RATIO)/2))
    }

    points.add(AimPoint(centerPt, radius, 0, 0.0))
    return points.toSet()
}

fun getColourForSegment(segment: DartboardSegment, colourWrapper: ColourWrapper?): Color
{
    val colourWrapperToUse = colourWrapper ?: getColourWrapperFromPrefs()
    return getColourFromHashMap(segment, colourWrapperToUse)
}

fun getColourFromHashMap(segment: DartboardSegment, colourWrapper: ColourWrapper): Color
{
    val type = segment.type
    if (type == SegmentType.MISS)
    {
        return colourWrapper.outerDartboardColour
    }

    if (type == SegmentType.MISSED_BOARD)
    {
        return colourWrapper.missedBoardColour
    }

    val score = segment.score
    val multiplier = segment.getMultiplier()

    if (score == 25)
    {
        return colourWrapper.getBullColour(multiplier)
    }

    return colourWrapper.getColour(multiplier, score)
}

fun getColourWrapperFromPrefs(): ColourWrapper
{
    if (colourWrapperFromPrefs != null)
    {
        return colourWrapperFromPrefs!!
    }

    val evenSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_SINGLE_COLOUR)
    val evenDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_DOUBLE_COLOUR)
    val evenTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_EVEN_TREBLE_COLOUR)
    val oddSingleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_SINGLE_COLOUR)
    val oddDoubleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_DOUBLE_COLOUR)
    val oddTrebleStr = PreferenceUtil.getStringValue(PREFERENCES_STRING_ODD_TREBLE_COLOUR)

    val evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr)
    val evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr)
    val evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr)

    val oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr)
    val oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr)
    val oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr)

    colourWrapperFromPrefs = ColourWrapper(evenSingle, evenDouble, evenTreble,
            oddSingle, oddDouble, oddTreble, evenDouble, oddDouble)

    return colourWrapperFromPrefs!!
}

private fun initialiseOrdinalHashMap() : MutableMap<Int, Boolean>
{
    val ret = mutableMapOf<Int, Boolean>()

    for (i in 0 until numberOrder.size - 1)
    {
        val even = i and 1 == 0
        ret[numberOrder[i]] = even
    }

    return ret
}

fun resetCachedDartboardValues()
{
    colourWrapperFromPrefs = null
}

fun getAllPossibleSegments(): List<DartboardSegment>
{
    val segments = mutableListOf<DartboardSegment>()
    for (i in 1..20)
    {
        segments.add(DartboardSegment(SegmentType.DOUBLE, i))
        segments.add(DartboardSegment(SegmentType.TREBLE, i))
        segments.add(DartboardSegment(SegmentType.OUTER_SINGLE, i))
        segments.add(DartboardSegment(SegmentType.INNER_SINGLE, i))
        segments.add(DartboardSegment(SegmentType.MISS, i))
        segments.add(DartboardSegment(SegmentType.MISSED_BOARD, i))
    }

    segments.add(DartboardSegment(SegmentType.OUTER_SINGLE, 25))
    segments.add(DartboardSegment(SegmentType.DOUBLE, 25))

    return segments.toList()
}

fun getAllNonMissSegments() = getAllPossibleSegments().filterNot { it.isMiss() }