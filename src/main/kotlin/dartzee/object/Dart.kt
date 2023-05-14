package dartzee.`object`

import dartzee.ai.AimDart
import dartzee.ai.IDart
import dartzee.core.util.DateStatics
import dartzee.game.ClockType
import java.sql.Timestamp

open class Dart(
        override val score: Int,
        override  val multiplier: Int,
        val segmentType: SegmentType = SegmentType.MISS): IDart
{
    var ordinal = -1

    //What the player's "score" was before throwing this dart.
    //Means what you'd think for X01
    //For Round the Clock, this'll be what they were to aim for
    var startingScore = -1

    //For RTC again
    var clockTargets: List<Int> = emptyList()

    //Never set on the DB. Used for in-game stats, and is just set to the round number.
    var roundNumber = -1

    var participantId: String = ""
    var gameId: String = ""
    var dtThrown: Timestamp = DateStatics.END_OF_TIME

    /**
     * Helpers
     */

    fun getGolfScore() = getGolfScore(roundNumber)

    fun getHitScore(): Int
    {
        return when(multiplier)
        {
            0 -> 0
            else -> score
        }
    }

    fun getSegmentTypeToAimAt(): SegmentType
    {
        return when (multiplier)
        {
            1 -> SegmentType.OUTER_SINGLE
            2 -> SegmentType.DOUBLE
            else -> SegmentType.TREBLE
        }
    }

    fun getGolfScore(target: Int): Int
    {
        return if (score != target)
        {
            5
        } else segmentType.getGolfScore()
    }

    fun toAimDart(): AimDart = AimDart(score, multiplier, segmentType)

    override fun hashCode(): Int
    {
        val prime = 31
        var result = 1
        result = prime * result + multiplier
        result = prime * result + ordinal
        result = prime * result + score
        return result
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other)
            return true
        if (other == null)
            return false
        if (other !is Dart)
            return false
        if (multiplier != other.multiplier)
            return false
        if (ordinal != other.ordinal)
            return false
        return (score == other.score)
    }

    override fun toString() = format()

    fun hitClockTarget(clockType: ClockType) = score == startingScore && isRightClockMultiplier(clockType)
    fun hitAnyClockTarget(clockType: ClockType): Boolean
    {
        return hitClockTarget(clockType) || (clockTargets.contains(score) && isRightClockMultiplier(clockType))
    }

    private fun isRightClockMultiplier(clockType: ClockType): Boolean =
        when (clockType)
        {
            ClockType.Doubles -> isDouble()
            ClockType.Trebles -> isTreble()
            ClockType.Standard -> multiplier > 0
        }
}

class DartNotThrown : Dart(-1, -1)