package dartzee.logging

import dartzee.helper.AbstractTest
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.SQLException

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

    @Test
    fun `Should include nested SQLExceptions`()
    {
        val innermostSqle = SQLException("Hard Disk Error")
        val innerSqle = SQLException("Permission Denied")
        innerSqle.nextException = innermostSqle
        val sqle = SQLException("Unable to drop table FOO")
        sqle.nextException = innerSqle

        val stackTrace = extractStackTrace(sqle)
        stackTrace.shouldContain("java.sql.SQLException: Unable to drop table FOO")
        stackTrace.shouldContain("Child: java.sql.SQLException: Permission Denied")
        stackTrace.shouldContain("Child: java.sql.SQLException: Hard Disk Error")
    }
    
    @Test
    fun `Should extract a string from a stack trace array`()
    {
        val stackTrace = arrayOf(StackTraceElement("SomeClass", "doStuff", "SomeClass.kt", 58), StackTraceElement("SomeClass", "maybeDoStuff", "SomeClass.kt", 40))

        val result = extractThreadStack(stackTrace)
        val lines = result.lines()
        lines[0] shouldBe "\tat SomeClass.doStuff(SomeClass.kt:58)"
        lines[1] shouldBe "\tat SomeClass.maybeDoStuff(SomeClass.kt:40)"
    }
}
