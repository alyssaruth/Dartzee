package dartzee.core.util

import dartzee.logging.CODE_EDT_FROZEN
import dartzee.utils.InjectedThings.logger
import javax.swing.SwingUtilities

private const val EDT_WAIT_TIME = 10000L

class EdtMonitor(private val waitTime: Long) : Runnable
{
    override fun run()
    {
        while (checkEdtResponsive())
        {
            Thread.sleep(waitTime)
        }
    }

    private fun checkEdtResponsive(): Boolean {
        var updated = false

        SwingUtilities.invokeLater {
            updated = true
        }

        Thread.sleep(waitTime)

        if (!updated)
        {
            logger.error(CODE_EDT_FROZEN, "EDT did not respond after ${waitTime}ms")
            dumpThreadStacks()
        }

        return updated
    }

    companion object
    {
        fun start(waitTime: Long = EDT_WAIT_TIME): Thread
        {
            return Thread(EdtMonitor(waitTime), "EDT Monitor").also { it.start() }
        }
    }
}