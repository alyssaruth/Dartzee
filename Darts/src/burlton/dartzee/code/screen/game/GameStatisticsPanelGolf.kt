package burlton.dartzee.code.screen.game

import burlton.desktopcore.code.util.MathsUtil
import burlton.dartzee.code.`object`.Dart
import java.util.stream.IntStream

open class GameStatisticsPanelGolf : GameStatisticsPanel()
{
    override fun addRowsToTable()
    {
        addRow(getScoreRow({ i -> i.min().asInt.toDouble() }, "Best Hole"))
        addRow(getScoreRow({ stream -> MathsUtil.round(stream.average().asDouble, 2) }, "Avg. Hole"))
        addRow(getScoreRow({ stream -> stream.max().asInt.toDouble() }, "Worst Hole"))
        addRow(getMissesRow())
        addRow(getGambleRow({ r -> getPointsSquandered(r) }, "Points Squandered"))
        addRow(getGambleRow({ r -> getPointsImproved(r) }, "Points Improved"))

        //addRow(arrayOfNulls(getRowWidth()))

        addRow(getScoreCountRow(1))
        addRow(getScoreCountRow(2))
        addRow(getScoreCountRow(3))
        addRow(getScoreCountRow(4))
        addRow(getScoreCountRow(5))

        table.setColumnWidths("150")
    }

    private fun getMissesRow(): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = "Miss %"

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val darts = getFlattenedDarts(playerName)
            val missDarts = darts.filter { d -> d.getGolfScore() == 5 }

            val misses = missDarts.size.toDouble()
            val percent = 100 * misses / darts.size

            row[i + 1] = MathsUtil.round(percent, 2)
        }

        return row
    }

    /**
     * Any round where you could have "banked" and ended on something higher.
     */
    private fun getGambleRow(f: (rnd: List<Dart>) -> Int, desc: String): Array<Any?>
    {
        val pointsSquandered = arrayOfNulls<Any>(getRowWidth())
        pointsSquandered[0] = desc
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = hmPlayerToDarts[playerName]!!

            pointsSquandered[i + 1] = rounds.stream().mapToInt { r -> f.invoke(r) }.sum()
        }

        return pointsSquandered
    }

    private fun getPointsSquandered(round: List<Dart>): Int
    {
        val finalScore = round.last().getGolfScore()
        val bestScore = round.stream().mapToInt { d -> d.getGolfScore() }.min().asInt

        return finalScore - bestScore
    }

    /**
     * A bit difficult to define. Some examples:
     *
     * 4-3-2. You've gambled twice, and gained 1 each time. So method should return 2.
     * 3-4-2. You've gambled the 3, stuffed it, then clawed it back. Method should return 1.
     * 5-5-1. You've not gambled anything. Method should return 0.
     * 4-3-5. You've stuffed it - there was a gain but it's gone. Method should return 0.
     * 4-2-3. You've gained 1 (and also lost 1). Method should return 1 for the original '4' gamble. I guess.
     */
    private fun getPointsImproved(round: List<Dart>): Int
    {
        val finalScore = round.last().getGolfScore()
        val bestScore = round.stream().mapToInt { d -> d.getGolfScore() }.min().asInt

        //This round is stuffed - points have been squandered, not gained! Or it's just 1 dart!
        if (finalScore > bestScore || round.size == 1)
        {
            return 0
        }

        //Filter out the 5s - they're not interesting.
        val roundWithoutMisses = round.filter { d -> d.getGolfScore() < 5 }
        if (roundWithoutMisses.isEmpty())
        {
            //Round is all misses, so nothing to do
            return 0
        }

        //Now get the first non-5. Result is the difference between this and where you ended up.
        val gambledScore = roundWithoutMisses.first().getGolfScore()
        return gambledScore - bestScore
    }


    private fun getScoreCountRow(score: Int): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = "" + score

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val darts = getCountedDarts(playerName)
            val dartsOfScore = darts.filter { d -> d.getGolfScore() == score }

            row[i + 1] = dartsOfScore.size
        }

        return row
    }

    private fun getScoreRow(f: (stream: IntStream) -> Double, desc: String): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = desc

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val countedDarts = getCountedDarts(playerName)
            val stream = countedDarts.stream().mapToInt { d -> d.getGolfScore() }
            row[i + 1] = f.invoke(stream)
        }

        return row
    }

    /**
     * Get the darts that were actually counted, i.e. the last of each round.
     */
    private fun getCountedDarts(playerName: String): List<Dart>
    {
        val rounds = hmPlayerToDarts[playerName]!!

        return rounds.map { r -> r.last() }
    }

    override fun getRankedRowsHighestWins(): MutableList<String>
    {
        return mutableListOf("Points Improved")
    }

    override fun getRankedRowsLowestWins(): MutableList<String>
    {
        return mutableListOf("Best Hole", "Avg. Hole", "Worst Hole", "Miss %", "Points Squandered")
    }

    override fun getHistogramRows(): MutableList<String>
    {
        return mutableListOf("1", "2", "3", "4", "5")
    }

    override fun getStartOfSectionRows(): MutableList<String>
    {
        return mutableListOf("Points Squandered", "1", "Best Game")
    }
}
