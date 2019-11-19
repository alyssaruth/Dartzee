package burlton.core.code.util

import kotlin.math.ceil


fun Int.ceilDiv(other: Int): Int
{
    return ceil(this.toDouble() / other).toInt()
}

class MathsUtil
{
    companion object
    {
        fun round(number: Double, decimalPlaces: Int): Double
        {
            val powerOfTen = Math.pow(10.0, decimalPlaces.toDouble())

            val rounded = Math.round(powerOfTen * number)

            return rounded / powerOfTen
        }

        @JvmStatic fun getPercentage(count: Int, total: Double): Double
        {
            return getPercentage(count.toDouble(), total)
        }
        fun getPercentage(count: Double, total: Double): Double
        {
            return if (count == 0.0)
            {
                0.0
            } else round(100 * count / total, 1)
        }
    }
}
