package burlton.core.test.util

import burlton.core.code.util.Debug
import burlton.core.code.util.DebugExtension
import burlton.core.code.util.DebugUncaughtExceptionHandler
import burlton.core.test.helper.AbstractTest
import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.core.test.helper.verifyNotCalled
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDebugUncaughtExceptionHandler: AbstractTest()
{
    @Test
    fun `Should not show an error for suppressed logs`()
    {
        val handler = DebugUncaughtExceptionHandler()

        val extension = mockk<DebugExtension>(relaxed = true)
        Debug.setDebugExtension(extension)

        val ex = Exception("javax.swing.plaf.FontUIResource cannot be cast to javax.swing.Painter")
        handler.uncaughtException(Thread.currentThread(), ex)

        exceptionLogged() shouldBe true
        verifyNotCalled { extension.exceptionCaught(any()) }
    }

    @Test
    fun `Should not suppress errors without a message`()
    {
        val handler = DebugUncaughtExceptionHandler()

        val extension = mockk<DebugExtension>(relaxed = true)
        Debug.setDebugExtension(extension)

        val ex = Exception()
        handler.uncaughtException(Thread.currentThread(), ex)

        exceptionLogged() shouldBe true
        verify { extension.exceptionCaught(true) }
    }

    @Test
    fun `Should not suppress errors with an unrecognised message`()
    {
        val handler = DebugUncaughtExceptionHandler()

        val extension = mockk<DebugExtension>(relaxed = true)
        Debug.setDebugExtension(extension)

        val ex = Exception("Argh")
        handler.uncaughtException(Thread.currentThread(), ex)

        exceptionLogged() shouldBe true
        verify { extension.exceptionCaught(true) }
    }

    @Test
    fun `Should log the thread that the exception occurred in`()
    {
        val t = Thread("Foo")

        val handler = DebugUncaughtExceptionHandler()

        val extension = mockk<DebugExtension>(relaxed = true)
        Debug.setDebugExtension(extension)

        val ex = Exception()
        handler.uncaughtException(t, ex)

        getLogs() shouldContain "UNCAUGHT EXCEPTION in thread Thread[Foo"
        exceptionLogged() shouldBe true
        verify { extension.exceptionCaught(true) }
    }
}