package dartzee.e2e

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.screen.stats.player.PlayerStatisticsScreen
import io.github.alyssaruth.swingtest.findChild
import io.github.alyssaruth.swingtest.findWindow
import io.github.alyssaruth.swingtest.flushEdt
import io.github.alyssaruth.swingtest.getChild
import io.github.alyssaruth.swingtest.waitForAssertion
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
