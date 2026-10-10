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

tailrec fun toRomanNumerals(value: Int, numeralsSoFar: String = ""): String =
    if (value >= 1000) toRomanNumerals(value - 1000, numeralsSoFar + "M")
    else if (value >= 900) toRomanNumerals(value - 900, numeralsSoFar + "CM")
    else if (value >= 500) toRomanNumerals(value - 500, numeralsSoFar + "D")
    else if (value >= 400) toRomanNumerals(value - 400, numeralsSoFar + "CD")
    else if (value >= 100) toRomanNumerals(value - 100, numeralsSoFar + "C")
    else if (value >= 90) toRomanNumerals(value - 90, numeralsSoFar + "XC")
    else if (value >= 50) toRomanNumerals(value - 50, numeralsSoFar + "L")
    else if (value >= 40) toRomanNumerals(value - 40, numeralsSoFar + "XL")
    else if (value >= 10) toRomanNumerals(value - 10, numeralsSoFar + "X")
    else if (value >= 9) numeralsSoFar + "IX"
    else if (value >= 5) toRomanNumerals(value - 5, numeralsSoFar + "V")
    else if (value >= 4) numeralsSoFar + "IV"
    else if (value >= 1) toRomanNumerals(value - 1, numeralsSoFar + "I") else numeralsSoFar

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
