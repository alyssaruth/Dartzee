package dartzee.bean

import dartzee.core.bean.ScrollTableOrdered
import dartzee.core.helper.FakeCollectionShuffler
import dartzee.core.util.CollectionShuffler
import dartzee.core.util.InjectedCore
import dartzee.core.util.TableUtil
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.spyk
import io.mockk.verify
import net.miginfocom.swing.MigLayout
import org.junit.Test
import javax.swing.JButton

class TestScrollTableOrdered: AbstractTest()
{
    override fun beforeEachTest() {
        super.beforeEachTest()

        InjectedCore.collectionShuffler = CollectionShuffler()
    }

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
        val mockShuffler = spyk<FakeCollectionShuffler>()

        InjectedCore.collectionShuffler = mockShuffler

        val table = setupTable()
        table.btnRandomize.doClick()

        verify { mockShuffler.shuffleCollection(any()) }
        table.getRowValues().shouldContainExactly("C", "B", "A")
    }

    @Test
    fun `Should not offset the standard buttons by default`()
    {
        val table = ScrollTableOrdered()
        table.getButtonConstraints(table.btnMoveUp) shouldBe "cell 0 0"
        table.getButtonConstraints(table.btnMoveDown) shouldBe "cell 0 1"
        table.getButtonConstraints(table.btnRandomize) shouldBe "cell 0 2"
    }

    @Test
    fun `Should offset standard buttons and support adding custom ones`()
    {
        val table = ScrollTableOrdered(1)
        table.getButtonConstraints(table.btnMoveUp) shouldBe "cell 0 1"
        table.getButtonConstraints(table.btnMoveDown) shouldBe "cell 0 2"
        table.getButtonConstraints(table.btnRandomize) shouldBe "cell 0 3"

        val newBtn = JButton()
        table.addButtonToOrderingPanel(newBtn, 0)
        table.getButtonConstraints(newBtn) shouldBe "cell 0 0"
    }

    private fun ScrollTableOrdered.getButtonConstraints(btn: JButton): Any
    {
        val layoutManager = panelOrdering.layout as MigLayout
        return layoutManager.getComponentConstraints(btn)
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