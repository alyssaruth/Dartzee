package e2e

import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.getChild
import dartzee.awaitCondition
import dartzee.screen.stats.player.PlayerStatisticsScreen
import java.awt.Window

fun awaitStatisticsScreen(): PlayerStatisticsScreen
{
    awaitCondition {

        println("****************")
        Window.getWindows().forEach {
            val hasStatsScrn = it.findChild<PlayerStatisticsScreen>() != null
            val isVisible = it.isVisible

            println("Window $it: hasStatsScrn: $hasStatsScrn, isVisible: $isVisible")
        }
        println("****************")

        getWindow { it.findChild<PlayerStatisticsScreen>() != null }?.isVisible == true
    }

    return getWindow { it.findChild<PlayerStatisticsScreen>() != null }!!.getChild()
}