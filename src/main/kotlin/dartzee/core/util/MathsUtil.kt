package dartzee.core.util

import kotlin.math.ceil
import kotlin.math.pow

fun Int.ceilDiv(other: Int) = ceil(this.toDouble() / other).toInt()

fun <T> Pair<Double, Double>.mapStepped(stepSize: Double, mapFunction: (Double) -> T): List<T> {
    val actualStep = (10 * stepSize).toInt()
    val ret = mutableListOf<T>()
    var current = first
    while (current < second) {
        ret.add(mapFunction(current))
        current = ((current * 10) + actualStep) / 10.0
    }

    return ret
}

object MathsUtil {
    fun round(number: Double, decimalPlaces: Int): Double {
        val powerOfTen = 10.0.pow(decimalPlaces.toDouble())

        val rounded = Math.round(powerOfTen * number)

        return rounded / powerOfTen
    }

    fun getPercentage(count: Number, total: Number, digits: Int = 1) =
        getPercentage(count.toDouble(), total.toDouble(), digits)

    fun getPercentage(count: Double, total: Double, digits: Int = 1): Double {
        return if (count == 0.0) {
            0.0
        } else round(100 * count / total, digits)
    }
}
