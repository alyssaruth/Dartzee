package dartzee.screen.stats.player.x01

import dartzee.*
import dartzee.helper.AbstractTest
import dartzee.helper.makeGameWrapper
import dartzee.helper.makeX01RoundsMap
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestStatisticsTabX01ThreeDartAverage: AbstractTest()
{
    @Test
    fun `Should calculate the average correctly across all games`()
    {
        val dartRounds = makeX01RoundsMap(501,
            listOf(drtTrebleTwenty, drtOuterFive, drtOuterOne), // 66
            listOf(drtInnerNineteen, drtOuterSeven, drtOuterThree) // 29
        )

        val dartRoundsTwo = makeX01RoundsMap(501,
            listOf(drtOuterFourteen, drtInnerEleven, drtTrebleFourteen), // 67
        )

        val g1 = makeGameWrapper(dartRounds = dartRounds)
        val g2 = makeGameWrapper(dartRounds = dartRoundsTwo)

        val tab = StatisticsTabX01ThreeDartAverage()
        tab.setFilteredGames(listOf(g1, g2), emptyList())
        tab.populateStats()

        tab.nfThreeDartAverage.text shouldBe "54.0"
    }
}