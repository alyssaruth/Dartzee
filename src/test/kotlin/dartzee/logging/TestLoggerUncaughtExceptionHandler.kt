package dartzee.logging

import dartzee.helper.AbstractTest
import dartzee.shouldContainKeyValues
import io.kotlintest.shouldBe
import org.junit.Test

class TestLoggerUncaughtExceptionHandler: AbstractTest()
{
    @Test
    fun `Should log a single WARN line for suppressed errors`()
    {
        val handler = LoggerUncaughtExceptionHandler()

        val message = "javax.swing.plaf.FontUIResource cannot be cast to javax.swing.Painter"
        val ex = Exception(message)
        handler.uncaughtException(Thread.currentThread(), ex)

        val log = verifyLog(CODE_UNCAUGHT_EXCEPTION, Severity.WARN)
        log.errorObject shouldBe null
        log.message shouldBe "Suppressing uncaught exception: $ex"
        log.shouldContainKeyValues(KEY_THREAD to Thread.currentThread().toString(), KEY_EXCEPTION_MESSAGE to message)
    }

    @Test
    fun `Should not suppress errors without a message`()
    {
        val handler = LoggerUncaughtExceptionHandler()

        val ex = Exception()
        handler.uncaughtException(Thread.currentThread(), ex)

        val log = verifyLog(CODE_UNCAUGHT_EXCEPTION, Severity.ERROR)
        log.errorObject shouldBe ex
        log.shouldContainKeyValues(KEY_THREAD to Thread.currentThread().toString(), KEY_EXCEPTION_MESSAGE to null)
        log.message shouldBe "Uncaught exception: $ex"
    }

    @Test
    fun `Should not suppress errors with an unrecognised message`()
    {
        val t = Thread("Foo")
        val handler = LoggerUncaughtExceptionHandler()

        val ex = Exception("Argh")
        handler.uncaughtException(t, ex)

        val log = verifyLog(CODE_UNCAUGHT_EXCEPTION, Severity.ERROR)
        log.errorObject shouldBe ex
        log.shouldContainKeyValues(KEY_THREAD to t.toString(), KEY_EXCEPTION_MESSAGE to "Argh")
        log.message shouldBe "Uncaught exception: $ex"
    }
}