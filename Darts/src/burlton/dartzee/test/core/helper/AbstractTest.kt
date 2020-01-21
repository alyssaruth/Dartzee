package burlton.dartzee.test.core.helper

import burlton.dartzee.code.core.util.Debug
import burlton.dartzee.code.core.util.DialogUtil
import burlton.dartzee.test.core.util.TestDebug
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before

private const val DEBUG_MODE = true
private var doneOneTimeSetup = false

abstract class AbstractTest
{
    private var doneClassSetup = false
    protected val dialogFactory = TestMessageDialogFactory()

    @Before
    fun oneTimeSetup()
    {
        if (!doneOneTimeSetup)
        {
            doOneTimeSetup()
            doneOneTimeSetup = true
        }

        doOneTimeDartsSetup()

        if (!doneClassSetup)
        {
            doClassSetup()
            doneClassSetup = true
        }

        beforeEachTest()
    }

    open fun doOneTimeDartsSetup() {}

    private fun doOneTimeSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.sendingEmails = false
        Debug.logToSystemOut = DEBUG_MODE

        Debug.debugExtension = TestDebugExtension()
        DialogUtil.init(dialogFactory)
    }

    open fun doClassSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.logToSystemOut = DEBUG_MODE
        DialogUtil.init(dialogFactory)
    }

    open fun beforeEachTest()
    {
        Debug.lastErrorMillis = -1
        Debug.initialise(TestDebug.SimpleDebugOutput())
        dialogFactory.reset()
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