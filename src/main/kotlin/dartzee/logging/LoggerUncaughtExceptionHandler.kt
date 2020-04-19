package dartzee.logging

import dartzee.utils.InjectedThings.logger
import java.lang.Thread.UncaughtExceptionHandler

class LoggerUncaughtExceptionHandler : UncaughtExceptionHandler
{
    override fun uncaughtException(arg0: Thread, arg1: Throwable)
    {
        if (isSuppressed(arg1))
        {
            //Still stack trace, but don't show an error message or email a log
            logger.warn(CODE_UNCAUGHT_EXCEPTION, "Suppressing uncaught exception: $arg1", KEY_THREAD to arg0.toString(), KEY_EXCEPTION_MESSAGE to arg1.message)
        }
        else
        {
            logger.error(CODE_UNCAUGHT_EXCEPTION, "Uncaught exception: $arg1", arg1, KEY_THREAD to arg0.toString())
        }
    }

    /**
     * Some exceptions just can't be prevented, for example some Nimbus L&F exceptions that aren't caused by threading
     * issues (I can see it's in the AWT thread)
     */
    private fun isSuppressed(t: Throwable): Boolean
    {
        val message = t.message ?: return false

        return message == "javax.swing.plaf.FontUIResource cannot be cast to javax.swing.Painter"
                || message == "javax.swing.plaf.nimbus.DerivedColor\$UIResource cannot be cast to javax.swing.Painter"
                || message == "javax.swing.plaf.DimensionUIResource cannot be cast to java.awt.Color"
    }
}
