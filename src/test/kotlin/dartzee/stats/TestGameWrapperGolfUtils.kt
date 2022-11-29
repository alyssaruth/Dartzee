package dartzee.stats

import dartzee.helper.AbstractTest
import dartzee.helper.GAME_WRAPPER_GOLF_9
import dartzee.helper.GAME_WRAPPER_GOLF_9_2
import dartzee.helper.GAME_WRAPPER_GOLF_9_EVEN_ROUNDS
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

        GAME_WRAPPER_GOLF_9.populateOptimalScorecardMaps(hm)
        GAME_WRAPPER_GOLF_9_2.populateOptimalScorecardMaps(hm)

        // Same score in game 2, but fewer darts
        getDataRowForHole(hm, 8).shouldContainExactly(2L, 3)

        // Worse score in game 2
        getDataRowForHole(hm, 3).shouldContainExactly(1L, 3, 4, 1)

        // Worse score in game 1
        getDataRowForHole(hm, 2).shouldContainExactly(2L, 4, 5, 4)
    }

    @Test
    fun `Optimal scorecard should cope with missing rounds (due to a team game)`()
    {
        val hm = makeOptimalScorecardStartingMap()

        GAME_WRAPPER_GOLF_9_EVEN_ROUNDS.populateOptimalScorecardMaps(hm)

        getDataRowForHole(hm, 1).shouldContainExactly(-1L, 5, 5, 5)
        getDataRowForHole(hm, 4).shouldContainExactly(1L, 2)
    }

    private fun getDataRowForHole(optimalHoleMap: MutableMap<Int, OptimalHoleStat>, hole: Int): List<Any>
    {
        val stat = optimalHoleMap.getValue(hole)

        val scores = stat.darts.map { it.getGolfScore() }
        return listOf(stat.localGameId) + scores
    }
}