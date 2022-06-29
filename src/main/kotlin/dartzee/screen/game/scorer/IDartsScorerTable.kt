package dartzee.screen.game.scorer

import dartzee.`object`.Dart

interface IDartsScorerTable : IScorerTable
{
    fun getNumberOfColumnsForAddingNewDart() = getNumberOfColumns() - 1

    fun addDartRound(darts: List<Dart>)
    {
        addRow(makeEmptyRow())

        darts.forEach(::addDart)
    }

    fun addDart(drt: Dart)
    {
        addDartToRow(model.rowCount - 1, drt)
    }

    private fun addDartToRow(rowNumber: Int, drt: Dart)
    {
        for (i in 0 until getNumberOfColumnsForAddingNewDart())
        {
            val currentVal = model.getValueAt(rowNumber, i)
            if (currentVal == null)
            {
                model.setValueAt(drt, rowNumber, i)
                return
            }
        }

        throw Exception("Trying to add dart to row $rowNumber but it's already full.")
    }
}