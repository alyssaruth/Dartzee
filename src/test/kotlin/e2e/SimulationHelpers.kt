package e2e

import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.getChild
import dartzee.awaitCondition
import dartzee.screen.stats.player.PlayerStatisticsScreen
import java.awt.Window

fun awaitStatisticsScreen(): PlayerStatisticsScreen
{
    awaitCondition {
        getWindow { it.findChild<PlayerStatisticsScreen>() != null } != null
    }

    return getWindow { it.findChild<PlayerStatisticsScreen>() != null }!!.getChild()
}