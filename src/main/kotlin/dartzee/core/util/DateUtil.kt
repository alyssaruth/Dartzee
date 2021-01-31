package dartzee.core.util

import dartzee.core.util.DateStatics.Companion.END_OF_TIME
import java.sql.Timestamp
import java.text.SimpleDateFormat


fun getSqlDateNow() = Timestamp(System.currentTimeMillis())

fun getEndOfTimeSqlString() = END_OF_TIME.getSqlString()

fun isEndOfTime(dt: Timestamp?) = dt?.equals(END_OF_TIME) ?: false

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

fun Timestamp.getSqlString(): String
{
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val formatted = dateFormat.format(this)
    return "'$formatted'"
}

fun getFileTimeString(): String
{
    val dt = getSqlDateNow()
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm")
    return dateFormat.format(dt)
}