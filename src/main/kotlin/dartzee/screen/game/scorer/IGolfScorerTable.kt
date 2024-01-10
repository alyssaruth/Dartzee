package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import java.awt.Color

const val ROUNDS_HALFWAY = 9
const val ROUNDS_FULL = 18
const val GOLF_SCORE_COLUMN = 4
const val GOLF_GAME_ID_COLUMN = 5

fun isScoreRow(row: Int) = row == ROUNDS_HALFWAY || row == ROUNDS_FULL + 1

fun getGolfScorerColour(score: Int, brightness: Double): Color {
    val hue =
        when (score) {
            4 -> 0.1f
            3 -> 0.2f
            2 -> 0.3f
            1 -> 0.5f
            else -> 0f
        }

    return Color.getHSBColor(hue, 1f, brightness.toFloat())
}

interface IGolfScorerTable : IDartsScorerTable {
    val fudgeFactor: Int

    fun populateTable(rounds: List<List<Dart>>) {
        var totalScore = 0
        rounds.forEachIndexed { ix, round ->
            val roundNumber = ix + 1

            addDartRound(round)

            val score = round.last().getGolfScore()
            totalScore += score
            model.setValueAt(score, model.rowCount - 1, GOLF_SCORE_COLUMN)

            if (roundNumber == 9 || roundNumber == 18) {
                val totalRow = arrayOf<Any?>(null, null, null, null, totalScore)
                model.addRow(totalRow)
            }
        }
    }

    override fun makeEmptyRow(): Array<Any?> {
        val emptyRow = super.makeEmptyRow()

        // Set the first column to be the round number
        val rowCount = model.rowCount
        emptyRow[0] = getTargetForRowNumber(rowCount)

        return emptyRow
    }

    private fun getTargetForRowNumber(row: Int): Int {
        if (row < ROUNDS_HALFWAY) {
            // Row 0 is 1, etc.
            return row + fudgeFactor + 1
        }

        if (row > ROUNDS_HALFWAY) {
            // We have an extra subtotal row to consider
            return row + fudgeFactor
        }

        throw Exception("Trying to get round target for the subtotal row")
    }
}
