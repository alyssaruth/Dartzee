package dartzee.logging

import dartzee.helper.AbstractTest
import io.kotlintest.matchers.string.shouldContain
import org.junit.Test

class TestLoggingUtils: AbstractTest()
{
    @Test
    fun `Should extract stack trace`()
    {
        var stackTrace = ""
        val runnable = {
            val t = Throwable("Boom.")
            stackTrace = extractStackTrace(t)
        }

        val thread = Thread(runnable)
        thread.start()
        thread.join()

        stackTrace shouldContain "java.lang.Throwable: Boom."
        stackTrace shouldContain "\tat dartzee.logging.TestLoggingUtils\$Should extract stack trace\$runnable\$1.invoke"
        stackTrace shouldContain "\tat java.base/java.lang.Thread.run"
    }

    @Test
    fun `Extracted stack should include cause`()
    {
        val cause = KotlinNullPointerException("Oh dear")
        val t = Throwable("boom", cause)

        val stackTrace = extractStackTrace(t)
        stackTrace shouldContain "java.lang.Throwable: boom"
        stackTrace shouldContain "Caused by: kotlin.KotlinNullPointerException: Oh dear"
    }
}
