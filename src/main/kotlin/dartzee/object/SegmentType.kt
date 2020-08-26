package dartzee.`object`

import dartzee.game.ClockType

enum class SegmentType
{
    DOUBLE,
    TREBLE,
    OUTER_SINGLE,
    INNER_SINGLE,
    MISS,
    MISSED_BOARD;

    fun getGolfScore() =
        when (this)
        {
            DOUBLE -> 1
            TREBLE -> 2
            INNER_SINGLE -> 3
            OUTER_SINGLE -> 4
            MISS, MISSED_BOARD -> 5
        }

    fun getMultiplier() =
        when (this)
        {
            DOUBLE -> 2
            TREBLE -> 3
            MISS, MISSED_BOARD -> 0
            OUTER_SINGLE, INNER_SINGLE -> 1
        }

    fun getRoughSize(score: Int): Int
    {
        if (score == 25) return if (this == DOUBLE) 137 else 716
        return when (this)
        {
            DOUBLE -> 441 //8820
            TREBLE -> 275 //5500
            OUTER_SINGLE -> 2464 //49280
            INNER_SINGLE -> 1586 //31720
            MISS -> MISS_FUDGE_FACTOR
            else -> 3321
        }
    }
}

fun getSegmentTypeForClockType(clockType: ClockType): SegmentType
{
    return when (clockType)
    {
        ClockType.Standard -> SegmentType.OUTER_SINGLE
        ClockType.Doubles -> SegmentType.DOUBLE
        ClockType.Trebles -> SegmentType.TREBLE
    }
}

/**
 * Hard-coded values based on counting the points in a 500x500 rendered dartboard.
 */
fun getRoughScoringArea(): Double = 96173.0 + MISS_FUDGE_FACTOR