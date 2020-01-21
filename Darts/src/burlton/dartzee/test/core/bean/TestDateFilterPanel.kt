package burlton.dartzee.test.core.bean

import burlton.dartzee.code.core.bean.DateFilterPanel
import burlton.dartzee.code.core.util.enableChildren
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp
import java.text.SimpleDateFormat

class TestDateFilterPanel: AbstractTest()
{
    private val fmt = SimpleDateFormat("dd/MM/yyyy")

    @Test
    fun `Should always be valid if disabled`()
    {
        val filterPanel = DateFilterPanel()
        filterPanel.cbDateFrom.date = fmt.parse("04/01/2020")
        filterPanel.cbDateTo.date = fmt.parse("01/01/2020")
        filterPanel.enableChildren(false)

        filterPanel.valid() shouldBe true
    }

    @Test
    fun `Should not be valid if start date is after end date`()
    {
        val filterPanel = DateFilterPanel()
        filterPanel.cbDateFrom.date = fmt.parse("04/01/2020")
        filterPanel.cbDateTo.date = fmt.parse("01/01/2020")

        filterPanel.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The 'date from' cannot be after the 'date to'")
    }

    @Test
    fun `Should be valid if the end date is on or after the start date`()
    {
        val filterPanel = DateFilterPanel()
        filterPanel.cbDateFrom.date = fmt.parse("01/01/2020")
        filterPanel.cbDateTo.date = fmt.parse("01/01/2020")
        filterPanel.valid() shouldBe true

        filterPanel.cbDateTo.date = fmt.parse("02/01/2020")
        filterPanel.valid() shouldBe true
    }

    @Test
    fun `Should provide a sensible filter description`()
    {
        val filterPanel = DateFilterPanel()

        filterPanel.cbDateFrom.date = fmt.parse("01/01/2020")
        filterPanel.cbDateTo.date = fmt.parse("07/11/2020")

        filterPanel.getFilterDesc() shouldBe "01/01/2020 - 07/11/2020"
    }

    @Test
    fun `Should provide converted SQL timestamps`()
    {
        val filterPanel = DateFilterPanel()

        filterPanel.cbDateFrom.date = fmt.parse("01/01/2020")
        filterPanel.cbDateTo.date = fmt.parse("07/11/2020")

        val sqlDtFrom = filterPanel.getSqlDtFrom()
        fmt.format(sqlDtFrom) shouldBe "01/01/2020"

        val sqlDtTo = filterPanel.getSqlDtTo()
        fmt.format(sqlDtTo) shouldBe "07/11/2020"
    }

    @Test
    fun `Should correctly filter SQL dates`()
    {
        val filterPanel = DateFilterPanel()
        filterPanel.cbDateFrom.date = fmt.parse("02/01/2020")
        filterPanel.cbDateTo.date = fmt.parse("04/01/2020")

        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-01 00:00:00")) shouldBe false
        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-01 23:59:59")) shouldBe false
        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-02 00:00:00")) shouldBe true
        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-03 00:00:00")) shouldBe true
        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-04 00:00:00")) shouldBe true
        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-04 23:59:59")) shouldBe false
        filterPanel.filterSqlDate(Timestamp.valueOf("2020-01-05 00:00:00")) shouldBe false
    }
}