package burlton.desktopcore.test.util

import burlton.desktopcore.code.util.*
import burlton.desktopcore.code.util.DateStatics.Companion.END_OF_TIME
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmptyString
import org.junit.Test
import java.sql.Timestamp
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDateUtil
{
    @Test
    fun testGetSqlDateNow()
    {
        val now = ZonedDateTime.now()

        val dtNow = getSqlDateNow()
        val convertedDtNow = dtNow.toLocalDateTime()

        assertEquals(now.year, convertedDtNow.year)
        assertEquals(now.month, convertedDtNow.month)
        assertEquals(now.dayOfMonth, convertedDtNow.dayOfMonth)
        assertEquals(now.hour, convertedDtNow.hour)
    }

    @Test
    fun testEndOfTimeStr()
    {
        val str = getEndOfTimeSqlString()

        assertThat(str, equalTo("'9999-12-31 00:00:00'"))
    }

    @Test
    fun testFormatHHMMSS()
    {
        val formattedOne = formatHHMMSS(18540000.4) //5h9m0s
        val formattedRounded = formatHHMMSS(18540999.9) //5h9m0s (it floors it)

        val formattedBigNumbers = formatHHMMSS(55751000.0) //15h29m11s


        assertThat(formattedOne, equalTo("05:09:00"))
        assertThat(formattedRounded, equalTo("05:09:00"))
        assertThat(formattedBigNumbers, equalTo("15:29:11"))
    }

    @Test
    fun testStripTimeComponent()
    {
        val calendarOrig = Calendar.getInstance()
        val dt = calendarOrig.time

        val strippedDt = stripTimeComponent(dt)
        val calendarStripped = Calendar.getInstance()
        calendarStripped.time = strippedDt

        assertThat(calendarOrig.get(Calendar.YEAR), equalTo(calendarStripped.get(Calendar.YEAR)))
        assertThat(calendarOrig.get(Calendar.MONTH), equalTo(calendarStripped.get(Calendar.MONTH)))
        assertThat(calendarOrig.get(Calendar.DAY_OF_MONTH), equalTo(calendarStripped.get(Calendar.DAY_OF_MONTH)))

        assertThat(calendarStripped.get(Calendar.HOUR_OF_DAY), equalTo(0))
        assertThat(calendarStripped.get(Calendar.MINUTE), equalTo(0))
        assertThat(calendarStripped.get(Calendar.SECOND), equalTo(0))
        assertThat(calendarStripped.get(Calendar.MILLISECOND), equalTo(0))
    }

    @Test
    fun testIsEndOfTime()
    {
        assertFalse(isEndOfTime(null))
        assertFalse(isEndOfTime(getSqlDateNow()))
        assertTrue(isEndOfTime(END_OF_TIME))
    }

    @Test
    fun testIsOnOrAfter()
    {
        val nowMillis = System.currentTimeMillis()
        val dtNow = Timestamp(nowMillis)
        val slightlyLater = Timestamp(nowMillis + 1)
        val slightlyBefore = Timestamp(nowMillis - 1)

        assertFalse(isOnOrAfter(null, dtNow))
        assertFalse(isOnOrAfter(dtNow, null))
        assertFalse(isOnOrAfter(null, null))

        assertTrue(isOnOrAfter(dtNow, dtNow))
        assertTrue(isOnOrAfter(getSqlDateNow(), dtNow))
        assertTrue(isOnOrAfter(END_OF_TIME, dtNow))
        assertTrue(isOnOrAfter(dtNow, slightlyBefore))

        assertFalse(isOnOrAfter(dtNow, slightlyLater))
    }

    @Test
    fun testTimestampFormats()
    {
        val millis = 1545733545000 //25/12/2018 10:25:45

        val timestamp = Timestamp(millis)

        assertThat(timestamp.formatAsDate(), equalTo("25/12/2018"))
        assertThat(timestamp.formatTimestamp(), equalTo("25-12-2018 10:25"))
    }

    @Test
    fun testEndOfTimeFormats()
    {
        assertThat(END_OF_TIME.formatAsDate(), isEmptyString)
        assertThat(END_OF_TIME.formatTimestamp(), isEmptyString)
    }
}