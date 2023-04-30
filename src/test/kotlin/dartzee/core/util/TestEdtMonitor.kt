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
    fun `Should keep running while the EDT is responsive, then stop and log an error when it freezes`()
    {
        val t = EdtMonitor.start(500)
        t.shouldBeAlive()

        SwingUtilities.invokeAndWait { }
        SwingUtilities.invokeAndWait { }
        SwingUtilities.invokeAndWait { }

        Thread.sleep(1000)

        verifyNoLogs(CODE_EDT_FROZEN)
        t.shouldBeAlive()

        SwingUtilities.invokeAndWait { Thread.sleep(2000) }
        verifyLog(CODE_EDT_FROZEN, Severity.ERROR)

        t.shouldNotBeAlive()
    }
}