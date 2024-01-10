package dartzee.core.bean

import javax.swing.JFormattedTextField
import javax.swing.text.NumberFormatter

/** Text field to contain integer values */
class NumberField constructor(min: Int? = null, max: Int? = null, val testId: String = "") :
    JFormattedTextField(NumberFormatter()) {
    init {
        if (min != null) {
            setMinimum(min)
        }

        if (max != null) {
            setMaximum(max)
        }
    }

    fun getDouble(): Double {
        val n = value ?: return 0.0

        return if (n is Double) n else (n as Int).toDouble()
    }

    fun getNumber(): Int {
        val n = value ?: return -1
        return n as Int
    }

    fun setMaximum(max: Int) {
        val nf = formatter as NumberFormatter
        nf.maximum = max
    }

    fun setMinimum(min: Int) {
        val nf = formatter as NumberFormatter
        nf.minimum = min
    }

    fun getMaximum() = (formatter as NumberFormatter).maximum
}
