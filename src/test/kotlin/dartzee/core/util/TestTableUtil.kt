package dartzee.core.util

import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestTableUtil: AbstractTest()
{
    @Test
    fun `Default model should return all column values`()
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Foo")
        model.getColumnValues(0).shouldBeEmpty()

        model.addRow(arrayOf("one"))
        model.getColumnValues(0).shouldContainExactly("one")

        model.addRow(arrayOf<Any?>(null))
        model.getColumnValues(0).shouldContainExactly("one", null)
    }

    @Test
    fun `Should set a list of column names`()
    {
        val model = TableUtil.DefaultModel()

        model.setColumnNames(listOf("A", "B", "C"))

        model.getColumnName(0) shouldBe "A"
        model.getColumnName(1) shouldBe "B"
        model.getColumnName(2) shouldBe "C"
    }

    @Test
    fun `Should support adding multiple rows at once`()
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Foo")

        val rowOne = arrayOf<Any>("1")
        val rowTwo = arrayOf<Any>("2")

        model.addRows(listOf(rowOne, rowTwo))

        model.getValueAt(0, 0) shouldBe "1"
        model.getValueAt(1, 0) shouldBe "2"
    }

    @Test
    fun `Should be able to clear all rows`()
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Foo")

        val rowOne = arrayOf<Any>("1")
        model.addRow(rowOne)
        model.clear()
        model.rowCount shouldBe 0
    }

    @Test
    fun `Should be able to clear an empty table model`()
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Foo")
        model.clear()
        model.rowCount shouldBe 0
    }
}