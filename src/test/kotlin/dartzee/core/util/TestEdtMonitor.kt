package dartzee.core.util

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_EDT_FROZEN
import dartzee.logging.Severity
import io.kotest.matchers.concurrent.shouldBeAlive
import io.kotest.matchers.concurrent.shouldNotBeAlive
import org.junit.jupiter.api.Test
import javax.swing.SwingUtilities

class TestEdtMonitor : AbstractTest()
{
    @Test
    fun `Should log an error, dump stacks and terminate if EDT does not respond`()
    {
        val t = EdtMonitor.start(500)

        SwingUtilities.invokeAndWait { Thread.sleep(2000) }
        verifyLog(CODE_EDT_FROZEN, Severity.ERROR)

        t.shouldNotBeAlive()
    }

    @Test
    fun `Should not complain and keep running if the EDT is responsive`()
    {
        val t = EdtMonitor.start(1000)
        t.shouldBeAlive()

        SwingUtilities.invokeAndWait { }
        SwingUtilities.invokeAndWait { }
        SwingUtilities.invokeAndWait { }

        Thread.sleep(2000)

        verifyNoLogs(CODE_EDT_FROZEN)
        t.shouldBeAlive()
    }
}