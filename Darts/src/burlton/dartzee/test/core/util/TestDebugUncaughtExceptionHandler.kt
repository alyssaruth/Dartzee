package burlton.dartzee.test.core.util

import burlton.dartzee.code.core.util.Debug
import burlton.dartzee.code.core.util.DebugExtension
import burlton.dartzee.code.core.util.DebugUncaughtExceptionHandler
import burlton.dartzee.test.core.helper.AbstractTest
import burlton.dartzee.test.core.helper.exceptionLogged
import burlton.dartzee.test.core.helper.getLogs
import burlton.dartzee.test.core.helper.verifyNotCalled
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDebugUncaughtExceptionHandler: AbstractTest()
{
    private val ext = Debug.debugExtension

    override fun afterEachTest()
    {
        super.afterEachTest()

        Debug.debugExtension = ext
    }

    @Test
    fun `Should not show an error for suppressed logs`()
    {
        val handler = DebugUncaughtExceptionHandler()

        val extension = mockk<DebugExtension>(relaxed = true)
        Debug.debugExtension = extension

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
        Debug.debugExtension = extension

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
        Debug.debugExtension = extension

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
        Debug.debugExtension = extension

        val ex = Exception()
        handler.uncaughtException(t, ex)

        getLogs() shouldContain "UNCAUGHT EXCEPTION in thread Thread[Foo"
        exceptionLogged() shouldBe true
        verify { extension.exceptionCaught(true) }
    }
}