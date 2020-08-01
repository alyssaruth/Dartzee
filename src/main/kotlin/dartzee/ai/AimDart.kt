package dartzee.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dartzee.`object`.SegmentType

@JsonIgnoreProperties("segmentType", "total")
data class AimDart(val score: Int, val multiplier: Int)
{
    fun getSegmentType() =
        when (multiplier)
        {
            1 -> SegmentType.OUTER_SINGLE
            2 -> SegmentType.DOUBLE
            else -> SegmentType.TREBLE
        }

    fun getTotal() = score * multiplier
}