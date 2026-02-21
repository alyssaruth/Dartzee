package dartzee.e2e

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.screen.stats.player.PlayerStatisticsScreen
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldNotBe
import java.awt.Window

fun AbstractTest.awaitStatisticsScreen(): PlayerStatisticsScreen {
    waitForAssertion {
        getLogRecordsSoFar().map { it.loggingCode }.shouldContain(CODE_SIMULATION_FINISHED)
        findWindow<Window> { it.findChild<PlayerStatisticsScreen>() != null } shouldNotBe null
    }

    flushEdt()
    return findWindow<Window> { it.findChild<PlayerStatisticsScreen>() != null }!!.getChild()
}
