package burlton.desktopcore.code.util

import java.sql.Timestamp

class DateStatics
{
    companion object
    {
        @JvmField val END_OF_TIME: Timestamp = Timestamp.valueOf("9999-12-31 00:00:00")
        @JvmField val START_OF_TIME: Timestamp = Timestamp.valueOf("1900-01-01 00:00:00")
    }
}
