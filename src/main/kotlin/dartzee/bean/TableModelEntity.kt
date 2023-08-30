package dartzee.bean

import dartzee.db.AbstractEntity
import javax.swing.table.DefaultTableModel

class TableModelEntity(entities: List<AbstractEntity<*>>) : DefaultTableModel()
{
    init
    {
        //Use the first entity to set up columns
        val entity = entities.first()
        val cols = entity.getColumns()
        for (col in cols)
        {
            addColumn(col)
        }

        //Now create the rows
        addRows(entities, cols)
    }

    private fun addRows(entities: List<AbstractEntity<*>>, columns: List<String>)
    {
        entities.forEach { entity ->
            val row = arrayOfNulls<Any>(columns.size)
            for (i in columns.indices)
            {
                row[i] = entity.getField(columns[i])
            }

            addRow(row)
        }
    }
}
