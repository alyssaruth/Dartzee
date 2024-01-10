package dartzee.logging

import dartzee.logging.exceptions.ApplicationFault
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.utils.InjectedThings.logger
import java.lang.Thread.UncaughtExceptionHandler

class LoggerUncaughtExceptionHandler : UncaughtExceptionHandler {
    override fun uncaughtException(arg0: Thread, arg1: Throwable) {
        if (isSuppressed(arg1)) {
            // Still stack trace, but don't show an error message or email a log
            logger.warn(
                CODE_UNCAUGHT_EXCEPTION,
                "Suppressing uncaught exception: $arg1",
                KEY_THREAD to arg0.toString(),
                KEY_EXCEPTION_MESSAGE to arg1.message
            )
        } else if (arg1 is WrappedSqlException) {
            logger.logSqlException(arg1.sqlStatement, arg1.genericStatement, arg1.sqlException)
        } else if (arg1 is ApplicationFault) {
            logger.error(
                arg1.loggingCode,
                "Uncaught exception: ${arg1.message}",
                arg1,
                KEY_THREAD to arg0.toString()
            )
        } else {
            logger.error(
                CODE_UNCAUGHT_EXCEPTION,
                "Uncaught exception: $arg1 in thread $arg0",
                arg1,
                KEY_THREAD to arg0.toString()
            )
        }
    }

    /**
     * Some exceptions just can't be prevented, for example some Nimbus L&F exceptions that aren't
     * caused by threading issues (I can see it's in the AWT thread)
     */
    private fun isSuppressed(t: Throwable): Boolean {
        val message = t.message ?: return false

        return message.contains("cannot be cast to class javax.swing.Painter") ||
            message.contains("UIResource cannot be cast to class java.awt.Color")
    }
}
