package burlton.core.code.util

object StringUtil
{
    fun convertOrdinalToText(position: Int): String
    {
        if (position < 0) return ""

        val suffixes = listOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        val remainder = position % 100
        return when (remainder)
        {
            11, 12, 13 -> "${position}th"
            else -> "${position}${suffixes[position % 10]}"
        }
    }
}