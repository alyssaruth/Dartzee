package dartzee.utils

import dartzee.core.util.mapStepped
import dartzee.`object`.ColourWrapper
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import java.awt.Canvas
import java.awt.Color
import java.awt.Font
import java.awt.Point

/**
 * Utilities for the Dartboard object.
 */
private val numberOrder = listOf(20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5, 20)

val hmScoreToOrdinal = initialiseOrdinalHashMap()
private var colourWrapperFromPrefs: ColourWrapper? = null

private const val RATIO_INNER_BULL = 0.038
private const val RATIO_OUTER_BULL = 0.094
private const val LOWER_BOUND_TRIPLE_RATIO = 0.582
private const val UPPER_BOUND_TRIPLE_RATIO = 0.629
private const val LOWER_BOUND_DOUBLE_RATIO = 0.953

const val UPPER_BOUND_DOUBLE_RATIO = 1.0
const val UPPER_BOUND_OUTSIDE_BOARD_RATIO = 1.3

fun getDartForSegment(segment: DartboardSegment): Dart
{
    val score = segment.score
    val multiplier = segment.getMultiplier()
    return Dart(score, multiplier, segment.type)
}

fun getNumbersWithinN(number: Int, n: Int): List<Int>
{
    val ix = numberOrder.indexOf(number)
    val range = ((ix-n)..(ix+n))

    return range.map { numberOrder[(it+20) % 20] }
}

fun getAdjacentNumbers(number: Int) = getNumbersWithinN(number, 1).filterNot { it == number }

fun computePointsForSegment(segment: DartboardSegment, centre: Point, radius: Double): Set<Point>
{
    if (segment.isMiss()) {
        return emptySet()
    }

    val score = segment.score
    return if (score == 25) {
        val radii = getRadiiForBull(segment.type, radius)
        generateSegment(segment, centre, radius, 0.0 to 360.0, 0.5, radii)
    } else {
        val (startAngle, endAngle) = getAnglesForScore(score)
        val radii = getRadiiForSegmentType(segment.type, radius)
        val angleStep = getAngleStepForSegmentType(segment.type)
        generateSegment(segment, centre, radius, startAngle.toDouble() - 0.1 to endAngle.toDouble() + 0.1, angleStep, radii)
    }
}

private fun getAngleStepForSegmentType(segmentType: SegmentType) =
    when (segmentType) {
        SegmentType.INNER_SINGLE -> 0.2
        SegmentType.TREBLE -> 0.15
        else -> 0.1
    }

private fun generateSegment(segment: DartboardSegment, centre: Point, radius: Double, angleRange: Pair<Double, Double>, angleStep: Double, radiusRange: Pair<Double, Double>): Set<Point> {
    val allPts = angleRange.mapStepped(angleStep) { angle ->
        radiusRange.mapStepped(0.8) { r ->
            translatePoint(centre, r, angle)
        }
    }.flatten().toSet()

    return allPts.filter { factorySegmentForPoint(it, centre, radius) == segment }.toSet()
}


fun computeEdgePoints(segmentPoints: Collection<Point>): Set<Point>
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

fun getRadiiForBull(segmentType: SegmentType, radius: Double): Pair<Double, Double> =
    when (segmentType) {
        SegmentType.DOUBLE -> Pair(0.0, radius * RATIO_INNER_BULL)
        SegmentType.OUTER_SINGLE -> Pair(radius * RATIO_INNER_BULL, radius * RATIO_OUTER_BULL)
        else -> throw IllegalArgumentException("Invalid segment type: $segmentType")
    }

fun getRadiiForSegmentType(segmentType: SegmentType, radius: Double): Pair<Double, Double> {
    val (lowerRatio, upperRatio) = getRatioBounds(segmentType)
    return Pair((radius * lowerRatio) - 0.5, (radius * upperRatio) + 0.5)
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

fun factorySegmentForPoint(dartPt: Point, centerPt: Point, radius: Double): DartboardSegment
{
    val distance = dartPt.distance(centerPt)
    val ratio = distance / radius

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

/**
 * 1) Calculate the radius from the center to our point
 * 2) Using the radius, work out whether this makes us a miss, single, double or treble
 */
private fun calculateTypeForRatioNonBullseye(ratioToRadius: Double) =
    when
    {
        ratioToRadius < LOWER_BOUND_TRIPLE_RATIO -> SegmentType.INNER_SINGLE
        ratioToRadius < UPPER_BOUND_TRIPLE_RATIO -> SegmentType.TREBLE
        ratioToRadius < LOWER_BOUND_DOUBLE_RATIO -> SegmentType.OUTER_SINGLE
        ratioToRadius < UPPER_BOUND_DOUBLE_RATIO -> SegmentType.DOUBLE
        else -> SegmentType.MISS
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
fun getPotentialAimPoints(centerPt: Point, radius: Double): Set<AimPoint>
{
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
    }

    segments.add(DartboardSegment(SegmentType.OUTER_SINGLE, 25))
    segments.add(DartboardSegment(SegmentType.DOUBLE, 25))

    return segments.toList()
}

fun getAllNonMissSegments() = getAllPossibleSegments().filterNot { it.isMiss() }

fun getFontForDartboardLabels(lblHeight: Int): Font
{
    //Start with a fontSize of 1
    var fontSize = 1f
    var font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, fontSize)

    //We're going to increment our test font 1 at a time, and keep checking its height
    var testFont = font
    var metrics = factoryFontMetrics(testFont)
    var fontHeight = metrics.height

    while (fontHeight < lblHeight - 2)
    {
        //The last iteration succeeded, so set our return value to be the font we tested.
        font = testFont

        //Create a new testFont, with incremented font size
        fontSize++
        testFont = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, fontSize)

        //Get the updated font height
        metrics = factoryFontMetrics(testFont)
        fontHeight = metrics.height
    }

    return font
}

fun factoryFontMetrics(font: Font) = Canvas().getFontMetrics(font)

fun getHighlightedColour(colour: Color): Color =
    if (colour == DartsColour.DARTBOARD_BLACK)
    {
        Color.DARK_GRAY
    }
    else
    {
        DartsColour.getDarkenedColour(colour)
    }