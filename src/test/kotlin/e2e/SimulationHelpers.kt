package e2e

import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.getChild
import dartzee.awaitCondition
import dartzee.screen.stats.player.PlayerStatisticsScreen

fun awaitStatisticsScreen(): PlayerStatisticsScreen
{
    awaitCondition { getWindow { it.findChild<PlayerStatisticsScreen>() != null }?.isVisible == true }
    return getWindow { it.findChild<PlayerStatisticsScreen>() != null }!!.getChild()
}