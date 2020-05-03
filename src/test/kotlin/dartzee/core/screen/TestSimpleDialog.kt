package dartzee.core.screen

import dartzee.helper.AbstractTest
import dartzee.helper.logger
import dartzee.logging.LoggingCode
import io.kotlintest.shouldBe
import org.junit.Test

class TestSimpleDialog: AbstractTest()
{
    var allowCancel = true

    @Test
    fun `Should show or hide the cancel button as appropriate`()
    {
        allowCancel = true
        val dlg = SimpleDialogTestExtension()
        dlg.btnCancel.isVisible shouldBe true

        allowCancel = false
        val dlg2 = SimpleDialogTestExtension()
        dlg2.btnCancel.isVisible shouldBe false
    }

    @Test
    fun `Pressing cancel should dispose the dialog by default`()
    {
        allowCancel = true

        val dlg = SimpleDialogTestExtension()
        dlg.isVisible = true

        dlg.btnCancel.doClick()

        dlg.isVisible shouldBe false
    }

    @Test
    fun `Pressing ok should do whatever has been implemented`()
    {
        val dlg = SimpleDialogTestExtension()

        dlg.btnOk.doClick()

        verifyLog(LoggingCode("OkPressed"))
    }

    inner class SimpleDialogTestExtension: SimpleDialog()
    {
        override fun okPressed()
        {
            logger.info(LoggingCode("OkPressed"), "pressed ok")
        }

        override fun allowCancel() = allowCancel

    }

}