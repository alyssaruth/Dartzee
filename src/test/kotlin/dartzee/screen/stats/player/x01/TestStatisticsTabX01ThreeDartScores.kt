package dartzee.screen.stats.player.x01

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.NumberField
import dartzee.core.bean.ScrollTable
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.GAME_WRAPPER_301_1
import dartzee.helper.GAME_WRAPPER_301_2
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestStatisticsTabX01ThreeDartScores: AbstractTest()
{
    @Test
    fun `Should show correct screen state for individual stats`()
    {
        val tab = StatisticsTabX01ThreeDartScores()
        tab.setFilteredGames(listOf(GAME_WRAPPER_301_1), emptyList())
        tab.populateStats()

        tab.myScores().shouldNotBeNull()
        tab.otherScores().shouldBeNull()
    }

    @Test
    fun `Should show correct screen state when a comparison is included`()
    {
        val tab = StatisticsTabX01ThreeDartScores()
        tab.setFilteredGames(listOf(GAME_WRAPPER_301_1), listOf(GAME_WRAPPER_301_2))
        tab.populateStats()

        tab.myScores().shouldNotBeNull()
        tab.otherScores().shouldNotBeNull()
    }

    @Test
    fun `Should respond correctly when threshold is updated`()
    {
        val tab = StatisticsTabX01ThreeDartScores()
        tab.setFilteredGames(listOf(GAME_WRAPPER_301_1, GAME_WRAPPER_301_2), emptyList())
        tab.populateStats()

        val scores = tab.myScores()!!.getRows().map { it[0] }
        scores.shouldContainExactlyInAnyOrder(120, 60, 57, 45)

        tab.getChild<NumberField>().value = 62
        val updatedScores = tab.myScores()!!.getRows().map { it[0] }
        updatedScores.shouldContainExactlyInAnyOrder(120, 60, 58, 57, 45, 1)
    }

    @Test
    fun `Breakdown table should update correctly when rows are selected from the summary`()
    {
        val tab = StatisticsTabX01ThreeDartScores()
        tab.getChild<NumberField>().value = 62
        tab.setFilteredGames(listOf(GAME_WRAPPER_301_1, GAME_WRAPPER_301_2), emptyList())
        tab.populateStats()

        val scoresTable = tab.myScores()!!
        val breakdownTable = tab.myBreakdown()!!

        scoresTable.selectRow(1)
        breakdownTable.getRows() shouldBe listOf(listOf("20, 20, 5", 2, 1L))

        scoresTable.selectRow(4)
        breakdownTable.getRows().shouldContainExactly(
            listOf("20, 20, 20", 1, 1L),
            listOf("T12, 20, 4", 1, 2L)
        )
    }

    private fun StatisticsTabX01ThreeDartScores.myScores() = findChild<ScrollTable> { it.testId == "PlayerScores" }
    private fun StatisticsTabX01ThreeDartScores.myBreakdown() = findChild<ScrollTable> { it.testId == "PlayerBreakdown" }
    private fun StatisticsTabX01ThreeDartScores.otherScores() = findChild<ScrollTable> { it.testId == "OtherScores" }
}