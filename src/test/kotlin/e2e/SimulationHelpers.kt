package e2e

import com.github.alyssaburlton.swingtest.awaitCondition
import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.screen.stats.player.PlayerStatisticsScreen

fun AbstractTest.awaitStatisticsScreen(): PlayerStatisticsScreen
{
    awaitCondition {
        getLogRecordsSoFar().any { it.loggingCode == CODE_SIMULATION_FINISHED } &&
                getWindow { it.findChild<PlayerStatisticsScreen>() != null } != null
    }

    flushEdt()
    return getWindow { it.findChild<PlayerStatisticsScreen>() != null }!!.getChild()
}