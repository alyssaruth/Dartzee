package dartzee.screen.game.scorer

import dartzee.core.util.TableUtil

interface IScorerTable {
    val model: TableUtil.DefaultModel

    fun getNumberOfColumns(): Int

    fun addRow(row: Array<*>) {
        model.addRow(row)
    }

    fun makeEmptyRow() = arrayOfNulls<Any>(getNumberOfColumns())
}
