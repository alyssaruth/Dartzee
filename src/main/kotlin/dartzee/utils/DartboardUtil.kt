package dartzee.utils

import dartzee.`object`.*
import dartzee.screen.Dartboard
import dartzee.utils.DartsColour.DARTBOARD_BLACK
import dartzee.utils.DartsColour.DARTBOARD_GREEN
import dartzee.utils.DartsColour.DARTBOARD_RED
import dartzee.utils.DartsColour.DARTBOARD_WHITE
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

fun getAdjacentNumbers(number: Int): MutableList<Int>
{
    if (number == 20)
    {
        return mutableListOf(1, 5)
    }

    val ix = numberOrder.indexOf(number)
    return mutableListOf(numberOrder[ix-1], numberOrder[ix+1])
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

fun getColourForPointAndSegment(pt: Point?, segment: DartboardSegment, colourWrapper: ColourWrapper?): Color
{
    val colourWrapperToUse = colourWrapper ?: getColourWrapperFromPrefs()

    val edgeColour = colourWrapperToUse.edgeColour
    if (edgeColour != null
            && !segment.isMiss()
            && segment.isEdgePoint(pt))
    {
        return edgeColour
    }

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

private fun getColourWrapperFromPrefs(): ColourWrapper
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

    val evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr, DARTBOARD_BLACK)
    val evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr, DARTBOARD_RED)
    val evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr, DARTBOARD_RED)

    val oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr, DARTBOARD_WHITE)
    val oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr, DARTBOARD_GREEN)
    val oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr, DARTBOARD_GREEN)

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

    Dartboard.appearancePreferenceChanged()
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