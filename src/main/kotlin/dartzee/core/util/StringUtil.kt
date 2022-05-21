package dartzee.core.util

object StringUtil
{
    fun convertOrdinalToText(position: Int): String
    {
        if (position < 0) return ""

        val suffixes = listOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (position % 100)
        {
            11, 12, 13 -> "${position}th"
            else -> "${position}${suffixes[position % 10]}"
        }
    }
}