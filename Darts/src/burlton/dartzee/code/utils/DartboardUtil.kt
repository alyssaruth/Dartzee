package burlton.dartzee.code.utils

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.screen.Dartboard
import java.awt.Color
import java.awt.Point

/**
 * Utilities for the Dartboard object.
 */
private val numberOrder = mutableListOf(20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5, 20)

private var hmScoreToOrdinal = initialiseOrdinalHashMap()
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

fun factorySegmentKeyForPoint(dartPt: Point, centerPt: Point, diameter: Double): String
{
    val radius = getDistance(dartPt, centerPt)
    val ratio = 2 * radius / diameter

    if (ratio < RATIO_INNER_BULL)
    {
        return "25_$SEGMENT_TYPE_DOUBLE"
    }
    else if (ratio < RATIO_OUTER_BULL)
    {
        return "25_$SEGMENT_TYPE_OUTER_SINGLE"
    }

    //We've not hit the bullseye, so do other calculations to work out score/multiplier
    val angle = getAngleForPoint(dartPt, centerPt)
    val score = getScoreForAngle(angle)
    val type = calculateTypeForRatioNonBullseye(ratio)

    return score.toString() + "_" + type
}

/**
 * 1) Calculate the radius from the center to our point
 * 2) Using the diameter, work out whether this makes us a miss, single, double or treble
 */
private fun calculateTypeForRatioNonBullseye(ratioToDiameter: Double): Int
{
    if (ratioToDiameter < LOWER_BOUND_TRIPLE_RATIO)
    {
        return SEGMENT_TYPE_INNER_SINGLE
    }
    else if (ratioToDiameter < UPPER_BOUND_TRIPLE_RATIO)
    {
        return SEGMENT_TYPE_TREBLE
    }
    else if (ratioToDiameter < LOWER_BOUND_DOUBLE_RATIO)
    {
        return SEGMENT_TYPE_OUTER_SINGLE
    }
    else if (ratioToDiameter < UPPER_BOUND_DOUBLE_RATIO)
    {
        return SEGMENT_TYPE_DOUBLE
    }
    else if (ratioToDiameter < UPPER_BOUND_OUTSIDE_BOARD_RATIO)
    {
        return SEGMENT_TYPE_MISS
    }

    return SEGMENT_TYPE_MISSED_BOARD
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

fun getColourForPointAndSegment(pt: Point?, segment: DartboardSegment, highlighted: Boolean,
                                colourWrapper: ColourWrapper?): Color?
{
    val colourWrapperToUse = colourWrapper ?: getColourWrapperFromPrefs()

    val edgeColour = colourWrapperToUse.edgeColour
    if (edgeColour != null
            && !segment.isMiss()
            && segment.isEdgePoint(pt))
    {
        return edgeColour
    }

    val colour = getColourFromHashMap(segment, colourWrapperToUse)
    return if (highlighted)
    {
        DartsColour.getDarkenedColour(colour)
    }
    else colour

}

private fun getColourFromHashMap(segment: DartboardSegment, colourWrapper: ColourWrapper): Color?
{
    val type = segment.type
    if (type == SEGMENT_TYPE_MISS)
    {
        return colourWrapper.outerDartboardColour
    }

    if (type == SEGMENT_TYPE_MISSED_BOARD)
    {
        return colourWrapper.missedBoardColour
    }

    val score = segment.score
    val multiplier = segment.getMultiplier()

    if (score == 25)
    {
        return colourWrapper.getBullColour(multiplier)
    }

    val even = hmScoreToOrdinal[Integer.valueOf(score)] ?: false
    return colourWrapper.getColour(multiplier, even)
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

    val evenSingle = DartsColour.getColorFromPrefStr(evenSingleStr, DartsColour.DARTBOARD_BLACK)
    val evenDouble = DartsColour.getColorFromPrefStr(evenDoubleStr, DartsColour.DARTBOARD_RED)
    val evenTreble = DartsColour.getColorFromPrefStr(evenTrebleStr, DartsColour.DARTBOARD_RED)

    val oddSingle = DartsColour.getColorFromPrefStr(oddSingleStr, DartsColour.DARTBOARD_WHITE)
    val oddDouble = DartsColour.getColorFromPrefStr(oddDoubleStr, DartsColour.DARTBOARD_GREEN)
    val oddTreble = DartsColour.getColorFromPrefStr(oddTrebleStr, DartsColour.DARTBOARD_GREEN)

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