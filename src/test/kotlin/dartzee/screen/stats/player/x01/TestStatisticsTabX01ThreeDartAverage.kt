package dartzee.screen.stats.player.x01

import dartzee.*
import dartzee.`object`.Dart
import dartzee.core.obj.HashMapList
import dartzee.helper.AbstractTest
import dartzee.helper.makeGameWrapper
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestStatisticsTabX01ThreeDartAverage: AbstractTest()
{
    @Test
    fun `Should calculate the average correctly across all games`()
    {
        // TODO - darts need to take into account startingScore in order to work properly with score threshold thing
        val dartRounds = HashMapList<Int, Dart>()
        dartRounds[1] = mutableListOf(drtTrebleTwenty, drtOuterFive, drtOuterOne) // 66
        dartRounds[2] = mutableListOf(drtInnerNineteen, drtOuterSeven, drtOuterThree) // 29

        val dartRoundsTwo = HashMapList<Int, Dart>()
        dartRoundsTwo[1] = mutableListOf(drtOuterFourteen, drtInnerEleven, drtTrebleFourteen) // 67

        val g1 = makeGameWrapper(dartRounds = dartRounds)
        val g2 = makeGameWrapper(dartRounds = dartRoundsTwo)

        val tab = StatisticsTabX01ThreeDartAverage()
        tab.setFilteredGames(listOf(g1, g2), emptyList())
        tab.populateStats()

        tab.nfThreeDartAverage.text shouldBe "54"

    }
}