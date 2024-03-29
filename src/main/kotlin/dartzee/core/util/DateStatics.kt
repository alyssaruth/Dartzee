package dartzee.core.util

import java.sql.Timestamp

object DateStatics {
    val END_OF_TIME: Timestamp = Timestamp.valueOf("9999-12-31 00:00:00")
    val START_OF_TIME: Timestamp = Timestamp.valueOf("1900-01-01 00:00:00")
}
