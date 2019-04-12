package burlton.desktopcore.test.helpers

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.core.test.helper.checkedForExceptions
import burlton.core.test.helper.exceptionLogged
import burlton.desktopcore.code.util.DialogUtil
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before

private const val DEBUG_MODE = true
private var doneOneTimeSetup = false

abstract class AbstractDesktopTest
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

    fun doOneTimeSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.setDebugExtension(TestDebugExtension())
        Debug.setSendingEmails(false)
        Debug.setLogToSystemOut(DEBUG_MODE)
        DialogUtil.init(dialogFactory)
    }

    open fun doClassSetup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.setLogToSystemOut(DEBUG_MODE)
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