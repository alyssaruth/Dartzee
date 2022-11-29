package dartzee.stats

import dartzee.helper.AbstractTest
import dartzee.helper.golfFrontNine22
import dartzee.helper.golfFrontNine22EvenRounds
import dartzee.helper.golfFrontNine29
import dartzee.screen.stats.player.golf.OptimalHoleStat
import dartzee.screen.stats.player.golf.makeOptimalScorecardStartingMap
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class TestGameWrapperGolfUtils : AbstractTest()
{
    @Test
    fun `Should populate optimal scorecard map correctly`()
    {
        val hm = makeOptimalScorecardStartingMap()

        golfFrontNine22(1L).populateOptimalScorecardMaps(hm)
        golfFrontNine29(2L).populateOptimalScorecardMaps(hm)

        // Same score in same number of darts, should be game 1
        getDataRowForHole(hm, 4).shouldContainExactly(1L, 5, 5, 4)

        // Same score in game 2, but fewer darts
        getDataRowForHole(hm, 2).shouldContainExactly(2L, 4)

        // Worse score in game 2
        getDataRowForHole(hm, 7).shouldContainExactly(1L, 5, 1)

        // Worse score in game 1
        getDataRowForHole(hm, 9).shouldContainExactly(2L, 4)
    }

    @Test
    fun `Optimal scorecard should cope with missing rounds (due to a team game)`()
    {
        val hm = makeOptimalScorecardStartingMap()

        golfFrontNine22EvenRounds().populateOptimalScorecardMaps(hm)

        getDataRowForHole(hm, 1).shouldContainExactly(-1L, 5, 5, 5)
        getDataRowForHole(hm, 4).shouldContainExactly(1L, 5, 5, 4)
    }

    private fun getDataRowForHole(optimalHoleMap: MutableMap<Int, OptimalHoleStat>, hole: Int): List<Any>
    {
        val stat = optimalHoleMap.getValue(hole)

        val scores = stat.darts.map { it.getGolfScore() }
        return listOf(stat.localGameId) + scores
    }
}