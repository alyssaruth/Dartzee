package burlton.desktopcore.code.bean

import javax.swing.JFormattedTextField
import javax.swing.text.NumberFormatter

/**
 * Text field to contain integer values
 */
class NumberField @JvmOverloads constructor(min: Int? = null, max: Int? = null) : JFormattedTextField(NumberFormatter())
{
    init
    {
        if (min != null)
        {
            setMinimum(min)
        }

        if (max != null)
        {
            setMaximum(max)
        }
    }

    fun getNumber(): Int
    {
        val n = value ?: return -1
        return n as Int
    }

    fun setMaximum(max: Int)
    {
        val nf = formatter as NumberFormatter
        nf.maximum = max
    }

    fun setMinimum(min: Int)
    {
        val nf = formatter as NumberFormatter
        nf.minimum = min
    }
}
