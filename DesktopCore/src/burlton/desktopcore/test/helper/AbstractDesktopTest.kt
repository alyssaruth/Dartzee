package burlton.desktopcore.test.helper

import burlton.desktopcore.code.util.Debug
import burlton.desktopcore.code.util.DialogUtil

private var doneOneTimeSetup = false

abstract class AbstractDesktopTest: AbstractTest()
{
    private var doneClassSetup = false
    protected val dialogFactory = TestMessageDialogFactory()

    override fun doOneTimeDesktopSetup()
    {
        if (!doneOneTimeSetup)
        {
            Debug.debugExtension = TestDebugExtension()
            DialogUtil.init(dialogFactory)
        }

        doneOneTimeSetup = true
    }

    override fun doClassSetup()
    {
        super.doClassSetup()
        DialogUtil.init(dialogFactory)
    }

    override fun beforeEachTest()
    {
        super.beforeEachTest()
        dialogFactory.reset()
    }
}