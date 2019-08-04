package burlton.dartzee.test.screen.stats.player.rtc

import burlton.dartzee.code.screen.stats.player.rtc.StatisticsTabRoundTheClockHitRate
import burlton.dartzee.test.screen.stats.player.AbstractStatsPieBreakdownTest

class TestStatisticsTabRoundTheClockHitRate: AbstractStatsPieBreakdownTest<StatisticsTabRoundTheClockHitRate>()
{
    override fun factoryTab() = StatisticsTabRoundTheClockHitRate()
    override fun getAllPossibilitiesForScores() = 1..10000
}