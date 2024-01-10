package dartzee.logging

import dartzee.helper.AbstractTest
import dartzee.logging.exceptions.ApplicationFault
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.shouldContainKeyValues
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.sql.SQLException
import org.junit.jupiter.api.Test

class TestLoggerUncaughtExceptionHandler : AbstractTest() {
    @Test
    fun `Should log a single WARN line for suppressed errors`() {
        val handler = LoggerUncaughtExceptionHandler()

        val message = "javax.swing.plaf.FontUIResource cannot be cast to class javax.swing.Painter"
        val ex = Exception(message)
        handler.uncaughtException(Thread.currentThread(), ex)

        val log = verifyLog(CODE_UNCAUGHT_EXCEPTION, Severity.WARN)
        log.errorObject shouldBe null
        log.message shouldBe "Suppressing uncaught exception: $ex"
        log.shouldContainKeyValues(
            KEY_THREAD to Thread.currentThread().toString(),
            KEY_EXCEPTION_MESSAGE to message
        )
    }

    @Test
    fun `Should not suppress errors without a message`() {
        val handler = LoggerUncaughtExceptionHandler()

        val ex = Exception()
        handler.uncaughtException(Thread.currentThread(), ex)

        val log = verifyLog(CODE_UNCAUGHT_EXCEPTION, Severity.ERROR)
        log.errorObject shouldBe ex
        log.shouldContainKeyValues(
            KEY_THREAD to Thread.currentThread().toString(),
            KEY_EXCEPTION_MESSAGE to null
        )
        log.message shouldContain "Uncaught exception: $ex"
    }

    @Test
    fun `Should not suppress errors with an unrecognised message`() {
        val t = Thread("Foo")
        val handler = LoggerUncaughtExceptionHandler()

        val ex = Exception("Argh")
        handler.uncaughtException(t, ex)

        val log = verifyLog(CODE_UNCAUGHT_EXCEPTION, Severity.ERROR)
        log.errorObject shouldBe ex
        log.shouldContainKeyValues(KEY_THREAD to t.toString(), KEY_EXCEPTION_MESSAGE to "Argh")
        log.message shouldContain "Uncaught exception: $ex"
    }

    @Test
    fun `Should log the code and message from an ApplicationFault`() {
        val t = Thread("Foo")
        val handler = LoggerUncaughtExceptionHandler()

        val ex = ApplicationFault(LoggingCode("some.error"), "Argh")
        handler.uncaughtException(t, ex)

        val log = verifyLog(LoggingCode("some.error"), Severity.ERROR)
        log.errorObject shouldBe ex
        log.shouldContainKeyValues(KEY_THREAD to t.toString(), KEY_EXCEPTION_MESSAGE to "Argh")
        log.message shouldBe "Uncaught exception: Argh"
    }

    @Test
    fun `Should log a WrappedSqlException correctly`() {
        val t = Thread("Foo")
        val handler = LoggerUncaughtExceptionHandler()

        val sqle = SQLException("Unable to select from table FOO", "State.ROLLBACK", 403)
        val ex =
            WrappedSqlException(
                "SELECT * FROM Foo WHERE Id = 'id'",
                "SELECT * FROM Foo WHERE Id = ?",
                sqle
            )
        handler.uncaughtException(t, ex)

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.errorObject shouldBe sqle
        log.shouldContainKeyValues(
            KEY_GENERIC_SQL to "SELECT * FROM Foo WHERE Id = ?",
            KEY_SQL to "SELECT * FROM Foo WHERE Id = 'id'",
            KEY_SQL_STATE to "State.ROLLBACK",
            KEY_ERROR_CODE to 403,
            KEY_EXCEPTION_MESSAGE to "Unable to select from table FOO"
        )
    }
}
