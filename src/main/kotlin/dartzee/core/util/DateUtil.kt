package dartzee.core.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate

fun getSqlDateNow() = Timestamp(System.currentTimeMillis())

fun getEndOfTimeSqlString() = DateStatics.END_OF_TIME.getSqlString()

fun isEndOfTime(dt: Timestamp?) = dt?.equals(DateStatics.END_OF_TIME) ?: false

fun Timestamp.formatTimestamp(): String {
    if (isEndOfTime(this)) {
        return ""
    }

    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
    return dateFormat.format(this)
}

fun Timestamp.toLocalDate(): LocalDate? =
    if (isEndOfTime(this)) null else toLocalDateTime().toLocalDate()

fun LocalDate?.toTimestamp(): Timestamp =
    if (this == null) DateStatics.END_OF_TIME else Timestamp.valueOf(atTime(0, 0))

fun Timestamp.formatAsDate(): String {
    if (isEndOfTime(this)) {
        return ""
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    return dateFormat.format(this)
}

fun Timestamp.getSqlString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val formatted = dateFormat.format(this)
    return "'$formatted'"
}

fun getFileTimeString(): String {
    val dt = getSqlDateNow()
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm")
    return dateFormat.format(dt)
}
