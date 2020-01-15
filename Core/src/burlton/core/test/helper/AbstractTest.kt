package burlton.core.test.helper

import burlton.core.code.util.Debug
import burlton.core.test.util.TestDebug
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before

private const val DEBUG_MODE = true
private var doneOneTimeSetup = false

abstract class AbstractTest
{
    private var doneClassSetup = false

    @Before
    fun oneTimeSetup()
    {
        if (!doneOneTimeSetup)
        {
            doOneTimeSetup()
            doneOneTimeSetup = true
        }

        doOneTimeDesktopSetup()
        doOneTimeDartsSetup()

        if (!doneClassSetup)
        {
            doClassSetup()
            doneClassSetup = true
        }

        beforeEachTest()
    }

    open fun doOneTimeDesktopSetup() {}
    open fun doOneTimeDartsSetup() {}

    private fun doOneTimeSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.sendingEmails = false
        Debug.logToSystemOut = DEBUG_MODE
    }

    open fun doClassSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.logToSystemOut = DEBUG_MODE
    }

    open fun beforeEachTest()
    {
        Debug.lastErrorMillis = -1
        Debug.initialise(TestDebug.SimpleDebugOutput())
    }

    @After
    open fun afterEachTest()
    {
        if (!checkedForExceptions)
        {
            exceptionLogged() shouldBe false
        }

        checkedForExceptions = false
    }
}