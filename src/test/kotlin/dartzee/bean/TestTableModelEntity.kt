package dartzee.bean

import dartzee.helper.AbstractTest
import dartzee.utils.DartsDatabaseUtil
import io.kotest.matchers.collections.shouldBeOneOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestTableModelEntity : AbstractTest() {
    @Test
    fun `Should have precisely the same column names and types as on the table`() {
        DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach {
            val tm = TableModelEntity(listOf(it))
            val entityCols = it.getColumns()

            for (i in 0 until tm.columnCount) {
                tm.getColumnName(i) shouldBe entityCols[i]
                tm.getColumnClass(i) shouldBeOneOf
                    (listOf(it.getFieldType(entityCols[i]), Any::class.java))
            }
        }
    }

    @Test
    fun `Should initialise with the correct values from the entity`() {
        DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach {
            val list = listOf(it)

            val tm = TableModelEntity(list)

            val cols = it.getColumns()
            for (col in cols) {
                val ix = tm.findColumn(col)

                tm.getValueAt(0, ix) shouldBe it.getField(col)
            }
        }
    }
}
