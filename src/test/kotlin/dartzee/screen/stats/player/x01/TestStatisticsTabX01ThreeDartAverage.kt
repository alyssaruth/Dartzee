package dartzee.screen.stats.player.x01

import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeVisible
import com.github.alexburlton.swingtest.shouldNotBeVisible
import dartzee.*
import dartzee.core.bean.NumberField
import dartzee.core.bean.ScrollTable
import dartzee.helper.AbstractTest
import dartzee.helper.makeGameWrapper
import dartzee.helper.makeX01RoundsMap
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.XYPlot
import org.junit.jupiter.api.Test

class TestStatisticsTabX01ThreeDartAverage: AbstractTest()
{
    @Test
    fun `Should show appropriate screen state for individual stats`()
    {
        val rounds = makeX01RoundsMap(501,
            listOf(drtOuterFourteen(), drtInnerEleven(), drtTrebleFourteen()), // 67
        )

        val g = makeGameWrapper(dartRounds = rounds)
        val tab = StatisticsTabX01ThreeDartAverage()
        tab.setFilteredGames(listOf(g), emptyList())
        tab.populateStats()

        tab.overallAverageOther().shouldNotBeVisible()
        tab.missPercentOther().shouldNotBeVisible()
    }

    @Test
    fun `Should show appropriate screen state when a comparison is included`()
    {
        val roundsMine = makeX01RoundsMap(501,
            listOf(drtTrebleTwenty(), drtOuterFive(), drtOuterOne()), // 66
            listOf(drtInnerNineteen(), drtOuterSeven(), drtOuterThree()), // 29
            listOf(drtMissTwenty(), drtDoubleTwenty(), drtInnerTwenty()) // 60
        )
        val gameMine = makeGameWrapper(dartRounds = roundsMine)

        val roundsOther = makeX01RoundsMap(501,
            listOf(drtOuterFive(), drtOuterOne(), drtTrebleOne()), // 9
            listOf(drtOuterOne(), drtOuterOne(), drtMissOne()) // 2
        )
        val gameOther = makeGameWrapper(dartRounds = roundsOther)

        val tab = StatisticsTabX01ThreeDartAverage()
        tab.setFilteredGames(listOf(gameMine), listOf(gameOther))
        tab.populateStats()

        tab.overallAverageOther().shouldBeVisible()
        tab.missPercentOther().shouldBeVisible()

        tab.overallAverage().text shouldBe "51.7"
        tab.overallAverageOther().text shouldBe "5.5"
        tab.missPercent().text shouldBe "11.1"
        tab.missPercentOther().text shouldBe "16.7"
    }

    @Test
    fun `Should populate correctly with multiple games`()
    {
        val dartRounds = makeX01RoundsMap(501,
            listOf(drtTrebleTwenty(), drtOuterFive(), drtOuterOne()), // 66
            listOf(drtInnerNineteen(), drtOuterSeven(), drtOuterThree()) // 29
        )

        val dartRoundsTwo = makeX01RoundsMap(501,
            listOf(drtOuterFourteen(), drtInnerEleven(), drtTrebleFourteen()), // 67
        )

        val g1 = makeGameWrapper(dartRounds = dartRounds)
        val g2 = makeGameWrapper(dartRounds = dartRoundsTwo)

        val tab = StatisticsTabX01ThreeDartAverage()
        tab.setFilteredGames(listOf(g1, g2), emptyList())
        tab.populateStats()

        tab.overallAverage().text shouldBe "54.0"

        val scrollTable = tab.getChild<ScrollTable>()
        scrollTable.getRows().shouldContainExactly(
            listOf(1, 47.5, 501, g1.localId),
            listOf(2, 67.0, 501, g2.localId)
        )

        val plot = tab.chart()
        val dataset = plot.getDataset(0)
        dataset.getY(0, 0) shouldBe 47.5
        dataset.getY(0, 1) shouldBe 67.0
    }

    @Test
    fun `Should respect changes to the scoring threshold`()
    {
        val rounds = makeX01RoundsMap(301,
            listOf(drtOuterFourteen(), drtInnerEleven(), drtTrebleFourteen()), // 67 | 234 remaining
            listOf(drtInnerNineteen(), drtOuterNineteen(), drtOuterFifteen()), // 53 | 181 remaining
            listOf(drtOuterTwenty(), drtOuterTwenty(), drtOuterOne()),         // 41 | 140 remaining
            listOf(drtOuterTwenty(), drtTrebleOne(), drtInnerFive()),          // 28 | 112 remaining
            listOf(drtTrebleTwenty(), drtOuterTwenty(), drtMissSixteen()),     // 80 |  32 remaining
            listOf(drtDoubleSixteen()) // Fin.
        )

        val g = makeGameWrapper(gameParams = "301", dartRounds = rounds)
        val tab = StatisticsTabX01ThreeDartAverage()
        tab.setFilteredGames(listOf(g), emptyList())
        tab.populateStats()

        tab.overallAverage().text shouldBe "53.7"

        tab.changeSetupThreshold(141)
        tab.overallAverage().text shouldBe "60.0"

        tab.changeSetupThreshold(115)
        tab.overallAverage().text shouldBe "47.3"
    }

    private fun StatisticsTabX01ThreeDartAverage.overallAverage() = getChild<NumberField> { it.testId == "overallAverage" }
    private fun StatisticsTabX01ThreeDartAverage.overallAverageOther() = getChild<NumberField> { it.testId == "overallAverageOther" }
    private fun StatisticsTabX01ThreeDartAverage.missPercent() = getChild<NumberField> { it.testId == "missPercent" }
    private fun StatisticsTabX01ThreeDartAverage.missPercentOther() = getChild<NumberField> { it.testId == "missPercentOther" }
    private fun StatisticsTabX01ThreeDartAverage.chart() = getChild<ChartPanel>().chart.plot as XYPlot
    private fun StatisticsTabX01ThreeDartAverage.changeSetupThreshold(threshold: Int) {
        val field = getChild<NumberField> { it.testId == "setupThreshold" }
        field.value = threshold
    }
}