package dartzee.core.screen

import dartzee.core.bean.ScrollTable
import dartzee.core.util.getAllChildComponentsForType
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestTableModelDialog: AbstractTest()
{
    @Test
    fun `Should display with the correct title and dialog properties`()
    {
        val table = ScrollTable()
        val tmd = TableModelDialog("Bah", table)

        tmd.title shouldBe "Bah"
        tmd.isModal shouldBe true
        tmd.allowCancel() shouldBe false

        tmd.getAllChildComponentsForType<ScrollTable>().shouldContainExactly(table)
    }

    @Test
    fun `Should pass through column widths to the ScrollTable`()
    {
        val st = mockk<ScrollTable>(relaxed = true)

        val tmd = TableModelDialog("Test", st)

        tmd.setColumnWidths("foo")

        verify{ st.setColumnWidths("foo") }
    }

    @Test
    fun `Should dispose when Ok is pressed`()
    {
        val tmd = TableModelDialog("Test", ScrollTable())

        val spy = spyk(tmd)
        spy.okPressed()

        verify{ spy.dispose() }
    }
}