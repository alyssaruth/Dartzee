package dartzee.core.util

import dartzee.core.util.DateStatics.Companion.END_OF_TIME
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp
import java.time.ZonedDateTime

class TestDateUtil: AbstractTest()
{
    @Test
    fun testGetSqlDateNow()
    {
        val now = ZonedDateTime.now()

        val dtNow = getSqlDateNow()
        val convertedDtNow = dtNow.toLocalDateTime()

        now.year shouldBe  convertedDtNow.year
        now.month shouldBe convertedDtNow.month
        now.dayOfMonth shouldBe convertedDtNow.dayOfMonth
        now.hour shouldBe convertedDtNow.hour
    }

    @Test
    fun testEndOfTimeStr()
    {
        val str = getEndOfTimeSqlString()

        str.shouldBe("'9999-12-31 00:00:00'")
    }

    @Test
    fun testIsEndOfTime()
    {
        isEndOfTime(null).shouldBeFalse()
        isEndOfTime(getSqlDateNow()).shouldBeFalse()
        isEndOfTime(END_OF_TIME).shouldBeTrue()
    }

    @Test
    fun `Should format a regular date as expected`()
    {
        val millis = 1545733545000 //25/12/2018 10:25:45

        val timestamp = Timestamp(millis)

        timestamp.formatAsDate() shouldBe "25/12/2018"
        timestamp.formatTimestamp() shouldBe "25-12-2018 10:25"
    }

    @Test
    fun `Should format end of time correctly`()
    {
        END_OF_TIME.formatAsDate().shouldBeEmpty()
        END_OF_TIME.formatTimestamp().shouldBeEmpty()
    }

    @Test
    fun `Should return a valid string for running SQL`()
    {
        val millis = 1545733545000 //25/12/2018 10:25:45
        val timestamp = Timestamp(millis)

        insertGame(dtFinish = timestamp)

        val sql = "SELECT COUNT(1) FROM Game WHERE dtFinish = ${timestamp.getSqlString()}"
        val result = mainDatabase.executeQueryAggregate(sql)
        result shouldBe 1
    }
}