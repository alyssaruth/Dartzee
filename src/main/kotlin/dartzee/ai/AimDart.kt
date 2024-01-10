package dartzee.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dartzee.`object`.SegmentType

interface IDart {
    val score: Int
    val multiplier: Int

    fun getTotal() = score * multiplier

    fun isDouble() = multiplier == 2

    fun isTreble() = multiplier == 3

    fun format(): String {
        if (multiplier == 0) {
            return "0"
        }

        var ret = ""
        if (isDouble()) {
            ret += "D"
        } else if (isTreble()) {
            ret += "T"
        }

        ret += score

        return ret
    }
}

@JsonIgnoreProperties("segmentType", "total", "double", "treble")
data class AimDart(
    override val score: Int,
    override val multiplier: Int,
    val segment: SegmentType? = null
) : IDart {
    fun getSegmentType() =
        segment
            ?: when (multiplier) {
                0 -> SegmentType.MISS
                1 -> SegmentType.OUTER_SINGLE
                2 -> SegmentType.DOUBLE
                else -> SegmentType.TREBLE
            }

    override fun toString() = format()
}
