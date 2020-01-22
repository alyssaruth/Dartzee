package dartzee.core.util

import dartzee.core.util.DateStatics.Companion.END_OF_TIME
import java.sql.Timestamp
import java.text.SimpleDateFormat


fun getSqlDateNow() : Timestamp
{
    return Timestamp(System.currentTimeMillis())
}

fun getEndOfTimeSqlString() : String
{
    return "'9999-12-31 00:00:00'"
}

fun formatHHMMSS(timePlayed: Double): String
{
    val seconds = Math.floor(timePlayed / 1000 % 60).toInt()
    val minutes = Math.floor(timePlayed / 60000 % 60).toInt()
    val hours = Math.floor(timePlayed / 3600000).toInt()

    var secondsStr = "" + seconds
    var minutesStr = "" + minutes
    var hoursStr = "" + hours

    if (seconds < 10)
    {
        secondsStr = "0$seconds"
    }

    if (minutes < 10)
    {
        minutesStr = "0$minutes"
    }

    if (hours < 10)
    {
        hoursStr = "0$hours"
    }

    return "$hoursStr:$minutesStr:$secondsStr"
}

fun isEndOfTime(dt: Timestamp?): Boolean
{
    return dt?.equals(END_OF_TIME) ?: false

}

fun isOnOrAfter(t1: Timestamp?, t2: Timestamp?): Boolean {
    return if (t1 == null || t2 == null) {
        false
    } else t1.after(t2) || t1.equals(t2)

}

fun Timestamp.formatTimestamp(): String
{
    if (isEndOfTime(this))
    {
        return ""
    }

    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
    return dateFormat.format(this)
}

fun Timestamp.formatAsDate() : String
{
    if (isEndOfTime(this))
    {
        return ""
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    return dateFormat.format(this)
}