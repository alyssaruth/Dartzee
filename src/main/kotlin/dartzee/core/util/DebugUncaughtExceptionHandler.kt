package dartzee.core.util

import java.lang.Thread.UncaughtExceptionHandler

class DebugUncaughtExceptionHandler : UncaughtExceptionHandler
{
    override fun uncaughtException(arg0: Thread, arg1: Throwable)
    {
        Debug.append("UNCAUGHT EXCEPTION in thread $arg0")

        if (isSuppressed(arg1))
        {
            //Still stack trace, but don't show an error message or email a log
            Debug.stackTraceSilently(arg1)
        }
        else
        {
            Debug.stackTrace(arg1)
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
