package burlton.dartzee.test.bean

import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.code.bean.ScrollTableOrdered
import burlton.desktopcore.code.util.TableUtil
import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Test
import java.util.*
import java.util.Collections.shuffle

class TestScrollTableOrdered: AbstractDartsTest()
{
    @Test
    fun `should move rows up`()
    {
        val scrollTable = setupTable()

        scrollTable.selectRow(2)

        scrollTable.btnMoveUp.doClick()
        scrollTable.getRowValues().shouldContainExactly("A", "C", "B")

        scrollTable.btnMoveUp.doClick()
        scrollTable.getRowValues().shouldContainExactly("C", "A", "B")
    }

    @Test
    fun `Should do nothing if trying to move the first row up`()
    {
        val scrollTable = setupTable()

        scrollTable.selectRow(0)

        scrollTable.btnMoveUp.doClick()
        scrollTable.getRowValues().shouldContainExactly("A", "B", "C")
    }

    @Test
    fun `should move rows down`()
    {
        val scrollTable = setupTable()

        scrollTable.selectRow(0)

        scrollTable.btnMoveDown.doClick()
        scrollTable.getRowValues().shouldContainExactly("B", "A", "C")

        scrollTable.btnMoveDown.doClick()
        scrollTable.getRowValues().shouldContainExactly("B", "C", "A")
    }

    @Test
    fun `Should do nothing if trying to move last row down`()
    {
        val scrollTable = setupTable()

        scrollTable.selectRow(2)

        scrollTable.btnMoveDown.doClick()
        scrollTable.getRowValues().shouldContainExactly("A", "B", "C")
    }

    @Test
    fun `Should do nothing if trying to move a row with no selection`()
    {
        val scrollTable = setupTable()

        scrollTable.btnMoveUp.doClick()
        scrollTable.getRowValues().shouldContainExactly("A", "B", "C")

        scrollTable.btnMoveDown.doClick()
        scrollTable.getRowValues().shouldContainExactly("A", "B", "C")
    }

    @Test
    fun `Should support programatically sorting the rows by something`()
    {
        val table = ScrollTableOrdered()
        val tm = TableUtil.DefaultModel()
        tm.addColumn("Value")
        table.model = tm

        data class TestRow(val str: String, val num: Int)

        val a4 = TestRow("A", 4)
        val b2 = TestRow("B", 2)
        val c3 = TestRow("C", 3)
        val d1 = TestRow("D", 1)


        table.addRow(arrayOf(a4))
        table.addRow(arrayOf(c3))
        table.addRow(arrayOf(d1))
        table.addRow(arrayOf(b2))

        table.reorderRows { (it[0] as TestRow).str }
        table.getRowValues().shouldContainExactly(a4, b2, c3, d1)

        table.reorderRows { (it[0] as TestRow).num }
        table.getRowValues().shouldContainExactly(d1, b2, c3, a4)
    }

    @Test
    fun `Should grab all the rows`()
    {
        val table = setupTable()
        table.getRowValues().shouldContainExactly("A", "B", "C")
    }

    @Test
    fun `Should scramble the row order`()
    {
        mockkStatic(Collections::class)

        val slot = slot<MutableList<*>>()
        every { shuffle(capture(slot)) } answers { slot.captured.reverse() }

        val table = setupTable()
        table.btnRandomize.doClick()

        table.getRowValues().shouldContainExactly("C", "B", "A")
    }

    private fun ScrollTableOrdered.getRowValues(): List<Any?> = getAllRows().map { it[0] }

    private fun setupTable(): ScrollTableOrdered
    {
        val table = ScrollTableOrdered()
        val tm = TableUtil.DefaultModel()
        tm.addColumn("Value")
        table.model = tm


        table.addRow(arrayOf("A"))
        table.addRow(arrayOf("B"))
        table.addRow(arrayOf("C"))

        return table
    }
}