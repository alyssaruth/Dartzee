package burlton.desktopcore.test.screen

import burlton.desktopcore.code.util.Debug
import burlton.desktopcore.code.util.DebugExtension
import burlton.desktopcore.test.helper.exceptionLogged
import burlton.desktopcore.test.helper.getLogs
import burlton.desktopcore.code.screen.BugReportDialog
import burlton.desktopcore.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestBugReportDialog: AbstractTest()
{
    private val ext = Debug.debugExtension

    override fun afterEachTest()
    {
        super.afterEachTest()

        Debug.debugExtension = ext
    }

    @Test
    fun `Should enforce a non-empty description`()
    {
        val dlg = BugReportDialog()

        dlg.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must enter a description.")
    }

    @Test
    fun `Should show a message if sending the email fails`()
    {
        val ext = mockk<DebugExtension>(relaxed = true)
        every { ext.sendEmail(any(), any()) } throws Exception("Not again")

        Debug.debugExtension = ext

        val dlg = BugReportDialog()
        dlg.descriptionField.text = "Foo"
        dlg.btnOk.doClick()

        dialogFactory.infosShown.shouldContainExactly("Unable to send bug report. Please check your internet connection and try again.")
        exceptionLogged() shouldBe true
        getLogs() shouldContain "Unable to send Bug Report. Exceptions follow."
    }

    @Test
    fun `Should send a bug report when Ok is pressed`()
    {
        val ext = mockk<DebugExtension>(relaxed = true)

        Debug.positionLastEmailed = 0
        Debug.debugExtension = ext

        val dlg = BugReportDialog()
        dlg.descriptionField.text = "Description"
        dlg.textPaneReplicationSteps.text = "Some steps"
        dlg.btnOk.doClick()

        verify { ext.sendEmail("BUG REPORT: Description - Alex", any()) }
        dialogFactory.infosShown.shouldContainExactly("Bug report submitted.")
    }
}