package dartzee.screen.stats.player.rtc

import com.github.alexburlton.swingtest.getChild
import dartzee.core.bean.ScrollTable
import dartzee.getRows
import dartzee.helper.GAME_WRAPPER_RTC_IN_ORDER
import dartzee.helper.GAME_WRAPPER_RTC_OUT_OF_ORDER
import dartzee.helper.makeClockGameWrapper
import dartzee.screen.stats.player.AbstractStatsPieBreakdownTest
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class TestStatisticsTabRoundTheClockHitRate: AbstractStatsPieBreakdownTest<StatisticsTabRoundTheClockHitRate>()
{
    override fun factoryTab() = StatisticsTabRoundTheClockHitRate()
    override fun getAllPossibilitiesForScores() = 1..1000
    override fun factoryGameWrapper() = makeClockGameWrapper()

    @Test
    fun `Should exclude games that were not in order`()
    {
        val tab = factoryTab()
        tab.setFilteredGames(listOf(GAME_WRAPPER_RTC_IN_ORDER, GAME_WRAPPER_RTC_OUT_OF_ORDER), emptyList())
        tab.populateStats()

        val breakdownTable = tab.getChild<ScrollTable> { it.testId == "BreakdownMine" }
        val rows = breakdownTable.getRows()
        rows[0].shouldContainExactly(1, 1, 0, 0, 0, 0, 0, 0, 1.0)
    }
}