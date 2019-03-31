package burlton.desktopcore.test.helpers

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.desktopcore.code.util.DialogUtil
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
        if (doneOneTimeSetup)
        {
            return
        }

        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.setDebugExtension(TestDebugExtension())
        Debug.setSendingEmails(false)
        Debug.setLogToSystemOut(DEBUG_MODE)
        DialogUtil.init(dialogFactory)

        doneOneTimeSetup = true
    }

    @Before
    fun beforeClassGeneric()
    {
        if (doneClassSetup)
        {
            return
        }

        Debug.initialise(TestDebug.SimpleDebugOutput())
        Debug.setLogToSystemOut(DEBUG_MODE)
        DialogUtil.init(dialogFactory)

        beforeClass()

        doneClassSetup = true
    }

    @Before
    open fun beforeClass() {}

    @Before
    open fun beforeEachTest()
    {
        Debug.lastErrorMillis = -1
        dialogFactory.reset()
    }
}