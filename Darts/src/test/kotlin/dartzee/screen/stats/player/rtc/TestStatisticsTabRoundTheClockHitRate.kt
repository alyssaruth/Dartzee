package dartzee.test.screen.stats.player.rtc

import dartzee.screen.stats.player.rtc.StatisticsTabRoundTheClockHitRate
import dartzee.test.screen.stats.player.AbstractStatsPieBreakdownTest

class TestStatisticsTabRoundTheClockHitRate: AbstractStatsPieBreakdownTest<StatisticsTabRoundTheClockHitRate>()
{
    override fun factoryTab() = StatisticsTabRoundTheClockHitRate()
    override fun getAllPossibilitiesForScores() = 1..10000
}