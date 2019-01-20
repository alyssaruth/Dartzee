package burlton.dartzee.code.`object`

import burlton.dartzee.code.db.CLOCK_TYPE_DOUBLES
import burlton.dartzee.code.db.CLOCK_TYPE_TREBLES
import burlton.dartzee.code.utils.DartsRegistry
import burlton.dartzee.code.utils.PreferenceUtil
import java.awt.Point

open class Dart @JvmOverloads constructor(
        var score: Int,
        var multiplier: Int,
        var pt: Point? = null,
        var segmentType: Int = -1) : DartsRegistry
{
    var ordinal = -1

    //What the player's "score" was before throwing this dart.
    //Means what you'd think for X01
    //For Round the Clock, this'll be what they were to aim for
    var startingScore = 1

    //Never set on the DB. Used for in-game stats, and is just set to the round number.
    private var golfHole = -1
    var participantId: Long = -1

    /**
     * Helpers
     */
    fun isDouble() = multiplier == 2
    fun isTriple() = multiplier == 3
    fun getTotal() = score * multiplier
    fun getGolfScore() = getGolfScore(golfHole)
    fun getX() = pt?.x
    fun getY() = pt?.y

    fun getHitScore(): Int
    {
        return when(multiplier)
        {
            0 -> 0
            else -> score
        }
    }

    fun getRendered(): String
    {
        if (multiplier == 0)
        {
            return "0"
        }

        var ret = ""
        if (isDouble())
        {
            ret += "D"
        }
        else if (isTriple())
        {
            ret += "T"
        }

        ret += score

        return ret
    }

    fun getSegmentTypeToAimAt(): Int
    {
        return when (multiplier)
        {
            1 -> TYPE_OUTER_SINGLE
            2 -> TYPE_DOUBLE
            else -> TYPE_TREBLE
        }
    }

    fun getGolfScore(target: Int): Int
    {
        return if (score != target)
        {
            5
        } else getGolfScoreForSegment(segmentType)

    }

    fun setGolfHole(hole: Int)
    {
        this.golfHole = hole
    }

    override fun hashCode(): Int
    {
        val prime = 31
        var result = 1
        result = prime * result + multiplier
        result = prime * result + ordinal
        result = prime * result + score
        return result
    }

    override fun equals(obj: Any?): Boolean
    {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (obj !is Dart)
            return false
        val other = obj as Dart?
        if (multiplier != other!!.multiplier)
            return false
        if (ordinal != other.ordinal)
            return false
        return (score == other.score)
    }

    override fun toString(): String
    {
        val showTotal = PreferenceUtil.getBooleanValue(DartsRegistry.PREFERENCES_BOOLEAN_DISPLAY_DART_TOTAL_SCORE)
        return if (showTotal)
        {
            "" + getTotal()
        }
        else getRendered()

    }

    fun hitClockTarget(clockType: String?): Boolean
    {
        if (clockType == CLOCK_TYPE_DOUBLES && !isDouble())
        {
            return false
        }

        return if (clockType == CLOCK_TYPE_TREBLES && !isTriple())
        {
            false
        }
        else score == startingScore && multiplier > 0

    }

    operator fun compareTo(other: Dart): Int
    {
        //If there's a strict inequality in total, then it's simple
        if (getTotal() > other.getTotal())
        {
            return 1
        }

        if (getTotal() < other.getTotal())
        {
            return -1
        }

        //Totals are equal. So now look at the multiplier.
        //I.e. T12 > D18 even though the totals are both 36.
        if (multiplier > other.multiplier)
        {
            return 1
        }

        return if (other.multiplier > multiplier)
        {
            -1
        }
        //Same total and same multiplier, so must be an equivalent dart
        else 0
    }
}

/**
 * Static methods
 */
fun factorySingle(score: Int): Dart
{
    return Dart(score, 1)
}

fun factoryDouble(score: Int): Dart
{
    return Dart(score, 2)
}

fun factoryTreble(score: Int): Dart
{
    return Dart(score, 3)
}
