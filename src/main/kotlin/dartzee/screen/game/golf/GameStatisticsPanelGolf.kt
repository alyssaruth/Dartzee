package dartzee.screen.game.golf

import dartzee.core.util.MathsUtil
import dartzee.game.UniqueParticipantName
import dartzee.game.state.GolfPlayerState
import dartzee.`object`.Dart
import dartzee.screen.game.AbstractGameStatisticsPanel

open class GameStatisticsPanelGolf: AbstractGameStatisticsPanel<GolfPlayerState>()
{
    override fun getRankedRowsHighestWins() = listOf("Points Improved")
    override fun getRankedRowsLowestWins() = listOf("Best Hole", "Avg. Hole", "Worst Hole", "Miss %", "Points Squandered")
    override fun getHistogramRows() = listOf("1", "2", "3", "4", "5")
    override fun getStartOfSectionRows() = listOf("Points Squandered", "1", "Best Game")

    override fun addRowsToTable()
    {
        addRow(getScoreRow("Best Hole") { it.minOrNull() } )
        addRow(getScoreRow("Avg. Hole") { MathsUtil.round(it.average(), 2) })
        addRow(getScoreRow("Worst Hole") { it.maxOrNull() })
        addRow(getMissesRow())
        addRow(getGambleRow({ r -> getPointsSquandered(r) }, "Points Squandered"))
        addRow(getGambleRow({ r -> getPointsImproved(r) }, "Points Improved"))

        addRow(getScoreCountRow(1))
        addRow(getScoreCountRow(2))
        addRow(getScoreCountRow(3))
        addRow(getScoreCountRow(4))
        addRow(getScoreCountRow(5))

        table.setColumnWidths("150")
    }

    private fun getMissesRow() = prepareRow("Miss %") { playerName ->
        val darts = getFlattenedDarts(playerName)
        val missDarts = darts.filter { d -> d.getGolfScore() == 5 }
        MathsUtil.getPercentage(missDarts.size, darts.size)
    }

    /**
     * Any round where you could have "banked" and ended on something higher.
     */
    private fun getGambleRow(f: (rnd: List<Dart>) -> Int, desc: String) = prepareRow(desc) { playerName ->
        val rounds = hmPlayerToDarts[playerName].orEmpty()
        rounds.sumOf { f(it) }
    }

    private fun getPointsSquandered(round: List<Dart>): Int
    {
        val finalScore = round.last().getGolfScore()
        val bestScore = round.map { it.getGolfScore() }.minOrNull() ?: finalScore

        return finalScore - bestScore
    }

    private fun getPointsImproved(round: List<Dart>): Int
    {
        val finalScore = round.last().getGolfScore()
        val bestScore = round.map { d -> d.getGolfScore() }.minOrNull() ?: finalScore

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


    private fun getScoreCountRow(score: Int) = getScoreRow("$score") { scores -> scores.count { it == score } }

    private fun getScoreRow(desc: String, f: (golfScores: List<Int>) -> Any?) = prepareRow(desc) { playerName ->
        val countedDarts = getCountedDarts(playerName)
        val scores = countedDarts.map { d -> d.getGolfScore() }
        f(scores)
    }

    /**
     * Get the darts that were actually counted, i.e. the last of each round.
     */
    private fun getCountedDarts(playerName: UniqueParticipantName): List<Dart>
    {
        val rounds = hmPlayerToDarts[playerName]!!

        return rounds.map { r -> r.last() }
    }
}
